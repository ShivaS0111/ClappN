# Core creation flow diagrams

Mermaid flows derived from ClappN controllers/services. Open this file in GitHub / VS Code / Notion for rendered diagrams.

**Common request pipeline (all secured APIs):**

```mermaid
flowchart LR
  A[Client] -->|JWT + optional X-Store-Id / X-Business-Id| B[JwtAuthenticationFilter]
  B --> C[UserScopeFilter<br/>load membership scope from DB]
  C --> D[@RequirePermission]
  D --> E[Controller / Service]
  E -->|403 if out of scope| F[Response]
```

---

## 1. Business creation

`POST /api/business` · permission `business.create`

```mermaid
flowchart TD
  A[Client: AddNewBusinessRequest] --> B{Authenticated?}
  B -->|No| Z1[401]
  B -->|Yes| C{Has business.create?}
  C -->|No| Z2[403]
  C -->|Yes| D[Map to Business domain<br/>set createdBy = current user]
  D --> E[Save Business]
  E --> F{Owner email already exists?}
  F -->|Yes| Z3[Error: Owner email already exists]
  F -->|No| G[Create User owner<br/>hash password]
  G --> H[Assign BUSINESS_OWNER role<br/>on user]
  H --> I[Create Membership + EmployeeProfile<br/>businessId, role BUSINESS_OWNER<br/>empty storeScopes = all stores]
  I --> J[Return BusinessDTO]
```

**Creates:** `business` + `user` (owner) + `membership` (employee API id) + owner profile.

---

## 2. Store creation

`POST /api/stores` · permission `store.create`

```mermaid
flowchart TD
  A[Client: AddNewStoreRequest<br/>storeName, businessId, ...] --> B{Auth + store.create?}
  B -->|Fail| Z[401 / 403]
  B -->|OK| C[Load Business by businessId]
  C --> D[Map to Store<br/>attach Business]
  D --> E[StoreService.save]
  E --> F{Caller can access business/store scope?}
  F -->|Typically validated in service layer| G[Persist Store]
  G --> H[201 StoreDTO]
```

**Depends on:** existing `businessId`.  
**After create:** business-scoped users see the new store automatically (empty `storeScopes`); store-scoped users only if that store is added to their membership scopes.

---

## 3. Master catalog → store offerings (big picture)

```mermaid
flowchart LR
  subgraph Platform / shared
    BT[Business Type]
    BP[Business Product<br/>/api/business-product]
    BS[Business Service<br/>/api/business-service]
  end
  subgraph Tenant
    B[Business]
    S[Store]
    SP[Store Offered Product]
    SS[Store Offered Service]
  end
  BT --> BP
  BT --> BS
  B --> S
  BP -->|link productId| SP
  BS -->|link serviceId| SS
  S --> SP
  S --> SS
```

---

## 4. Store offered product creation

`POST /api/store-product/save` · permission `store_product.create`  
(bulk: `POST /api/store-product/add-all`)

```mermaid
flowchart TD
  A[Client: AddNewStoreOfferedProductRequest<br/>storeId + master product ref + ...] --> B{Auth + store_product.create?}
  B -->|Fail| Z1[401 / 403]
  B -->|OK| C[Map to StoreOfferedProduct]
  C --> D{storeId present?}
  D -->|Yes| E[validateStoreAccess storeId]
  E -->|Denied| Z2[403]
  E -->|OK| F[set createdBy = current userId]
  D -->|No| F
  F --> G[Save store_offered_product]
  G --> H[201 StoreOfferedProductDTO]
```

**Prerequisite:** master product usually exists in `/api/business-product`.  
**Scope:** store must be in caller’s accessible stores (or admin).

---

## 5. Store offered service creation

`POST /api/store-service/save` · permission `store_service.create`  
(bulk: `POST /api/store-service/add-all`)

```mermaid
flowchart TD
  A[Client: AddNewStoreOfferedServiceRequest<br/>storeId + master service ref + ...] --> B{Auth + store_service.create?}
  B -->|Fail| Z1[401 / 403]
  B -->|OK| C[Map to StoreOfferedService]
  C --> D[validateStoreAccess storeId]
  D -->|Denied| Z2[403]
  D -->|OK| E[set createdBy]
  E --> F[Save store_offered_service]
  F --> G[201 StoreOfferedServiceDTO]
```

Same pattern as store products.

---

## 6. Employee creation

`POST /api/employees` · permission `user.create`

```mermaid
flowchart TD
  A[Client: EmployeeRequest] --> B{Auth + user.create?}
  B -->|Fail| Z1[401 / 403]
  B -->|OK| C{businessId or storeId?}
  C -->|Neither| Z2[400]
  C -->|OK| D{userId set?}
  D -->|No| E{email provided?}
  E -->|No| Z3[400 userId or email required]
  E -->|Yes| F{User exists by email?}
  F -->|Yes| G[Reuse userId]
  F -->|No| H[Create User + temp password]
  H --> I[Email invite via MailService]
  I --> G
  D -->|Yes| G
  G --> J[EmployeeService.createEmployee]
  J --> K[validateBusinessAccess / validateStoreAccess]
  K -->|Denied| Z4[403]
  K -->|OK| L[Find or create Membership<br/>userId + businessId]
  L --> M[Attach role + storeScopes]
  M --> N[Upsert EmployeeProfile HR fields]
  N --> O[Return EmployeeResponse<br/>id = membership id]
```

**Login:** same `POST /api/auth/login` as any user (email + password). No separate employee login.

---

## 7. Recommended onboarding sequence

```mermaid
sequenceDiagram
  actor Admin as SYSTEM_ADMIN / creator
  participant Auth as /api/auth
  participant Biz as /api/business
  participant Store as /api/stores
  participant Cat as /api/business-product<br/>/api/business-service
  participant Offer as /api/store-product<br/>/api/store-service
  participant Emp as /api/employees

  Admin->>Auth: login
  Auth-->>Admin: JWT
  Admin->>Biz: create business + owner
  Biz-->>Admin: businessId (+ owner membership)
  Admin->>Store: create store businessId
  Store-->>Admin: storeId
  Admin->>Cat: create master products/services optional
  Admin->>Offer: link offerings to storeId
  Admin->>Emp: create cashier/manager<br/>user/email + storeId + roleId
  Note over Emp: Creates User if needed + Membership + Profile
```

---

## Quick reference

| Flow | API | Permission | Main tables |
|------|-----|------------|-------------|
| Business | `POST /api/business` | `business.create` | business, user, membership, employee_profile |
| Store | `POST /api/stores` | `store.create` | store |
| Store product | `POST /api/store-product/save` | `store_product.create` | store_offered_product |
| Store service | `POST /api/store-service/save` | `store_service.create` | store_offered_service |
| Employee | `POST /api/employees` | `user.create` | user?, membership, employee_profile |
