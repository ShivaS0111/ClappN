# ClappN API Catalog — Endpoints with Sample Input / Output

Generated from controllers under `src/main/java/.../feature`.  
**~219 route mappings** across **34 controllers**.

Base URL (local): `http://localhost:8080`  
Staging (docs): configure via `STAGING_BASE_URL`

---

## Common conventions

### Headers

```http
Authorization: Bearer <jwt>
Content-Type: application/json
Accept: application/json
X-Store-Id: 1          # optional — narrow to one store (must be in membership)
X-Business-Id: 10      # optional — narrow to one business
```

### Success envelope (`APIResponse`)

```json
{
  "success": true,
  "message": "Success",
  "statusCode": 200,
  "timestamp": "2026-09-20T12:00:00",
  "data": { }
}
```

### Error envelope

```json
{
  "success": false,
  "message": "You do not have access to store: 99",
  "statusCode": 403,
  "timestamp": "2026-09-20T12:00:00",
  "data": null
}
```

Typical status codes: `200` OK, `201` Created, `400` validation, `401` unauthenticated, `403` permission/scope denied, `404` not found.

---

## 1. Auth (`/api/auth`) — mostly public

| Method | Path | Auth | Permission |
|--------|------|------|------------|
| POST | `/api/auth/register` | public | — |
| POST | `/api/auth/login` | public | — |
| POST | `/api/auth/refresh-token` | public | — |
| POST | `/api/auth/logout` | public | — |
| POST | `/api/auth/forgot-password` | public | — |
| POST | `/api/auth/reset-password` | public | — |
| POST | `/api/auth/change-password` | JWT | authenticated |
| POST | `/api/auth/validate` | JWT | authenticated |
| POST | `/api/auth/revoke-all-refresh-tokens/{username}` | JWT | `user.permissions` |

### Sample — Login

**Request**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "store.manager@acme.com",
  "password": "Test@12345"
}
```

**Response `200`**
```json
{
  "success": true,
  "message": "Success",
  "statusCode": 200,
  "data": {
    "user": {
      "id": 12,
      "email": "store.manager@acme.com",
      "fullName": "Store Manager"
    },
    "tokenInfo": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "refreshToken": "a1b2c3d4-...",
      "tokenExpiry": 3600000
    }
  }
}
```

### Sample — Change password

**Request**
```json
{
  "currentPassword": "Old@12345",
  "newPassword": "New@12345"
}
```

---

## 2. Me / navigation

| Method | Path | Permission |
|--------|------|------------|
| GET | `/api/me/context` | auth only |
| GET | `/api/navigation-config` | auth only |

### Sample — Me context

**Request**
```http
GET /api/me/context
Authorization: Bearer <jwt>
X-Store-Id: 1
```

**Response `200`**
```json
{
  "success": true,
  "message": "Current user context",
  "statusCode": 200,
  "data": {
    "userId": 12,
    "email": "store.manager@acme.com",
    "roles": ["STORE_MANAGER"],
    "permissions": ["store.read", "order.create", "order.read", "..."],
    "accessibleStoreIds": [1],
    "accessibleBusinessIds": [10],
    "activeStoreId": 1,
    "activeBusinessId": null,
    "unrestricted": false
  }
}
```

`SYSTEM_ADMIN` → `unrestricted: true`, store/business id lists `null`.

---

## 3. Business (`/api/business`) — `business.*`

| Method | Path | Permission |
|--------|------|------------|
| GET | `/api/business` | `business.read` |
| GET | `/api/business/list` | `business.read` |
| GET | `/api/business/{id}` | `business.read` |
| POST | `/api/business/search` | `business.read` |
| POST | `/api/business` | `business.create` |
| PUT | `/api/business/{id}` | `business.update` |
| POST | `/api/business/update-status` | `business.update` |

### Sample — List (paged)

**Request:** `GET /api/business?page=0&size=10&status=1&keyword=acme`

**Response `data`**
```json
{
  "content": [
    {
      "id": 10,
      "businessName": "Acme Retail",
      "status": 1,
      "email": "ops@acme.com"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 10
}
```

### Sample — Create

**Request**
```json
{
  "businessName": "Acme Retail",
  "description": "Demo business",
  "email": "ops@acme.com",
  "ownerName": "Owner",
  "ownerEmail": "owner@acme.com",
  "ownerPhone": "+15550001111",
  "ownerPassword": "Temp@12345"
}
```

**Response:** `data` = created `BusinessDTO`.

Scope: business users only see **their** businesses; store users typically lack `business.read` or get 403 on foreign ids.

---

## 4. Stores (`/api/stores`) — `store.*`

| Method | Path | Permission |
|--------|------|------------|
| GET | `/api/stores` | `store.read` |
| GET | `/api/stores/list` | `store.read` |
| GET | `/api/stores/{storeId}` | `store.read` |
| GET | `/api/stores/{storeId}/details` | `store.read` |
| GET | `/api/stores/store-info/{storeId}` | `store.read` |
| GET | `/api/stores/list/{businessId}` | `store.read` |
| GET | `/api/stores/{storeId}/metrics` | `store.metrics` |
| POST | `/api/stores/search` | `store.read` |
| POST | `/api/stores` | `store.create` |
| PUT | `/api/stores/{id}` | `store.update` |
| DELETE | `/api/stores/{id}` | `store.delete` |
| POST | `/api/stores/update-status` | `store.update` |

### Sample — Get store

**Request:** `GET /api/stores/1` + `X-Store-Id: 1`

**Response `data`**
```json
{
  "id": 1,
  "storeName": "Acme Downtown",
  "businessId": 10,
  "status": 1,
  "address": "1 Main St",
  "email": "downtown@acme.com",
  "phone": "+15550002222"
}
```

### Sample — Create store

**Request**
```json
{
  "storeName": "Acme Mall",
  "businessId": 10,
  "description": "Mall kiosk",
  "address": "Mall Rd",
  "email": "mall@acme.com",
  "phone": "+15550003333"
}
```

### Sample — Update status

**Request**
```json
{ "id": 1, "status": 0 }
```

Store-scoped users: only assigned stores. Business-scoped: all stores under their business.

---

## 5. Shared catalog (not tenant-isolated)

### Brands — `brand.*` → `/api/brands`
GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

**Create body**
```json
{ "name": "Nike", "description": "Brand" }
```

### Categories — `category.*` → `/api/categories`
List/tree/add/update/delete (see controller).

**Add body**
```json
{ "name": "Shoes", "parentId": null, "businessTypeId": 1, "status": 1 }
```

### Business types — `/api/business-type`
List/CRUD (permission typically `business.read` / admin).

### Master products — `product.*` → `/api/business-product`
| Method | Path |
|--------|------|
| GET | `/list`, `/{productId}`, `/listByBusinessType/{id}` |
| POST | `/search`, `/`, `/add-all`, … |
| PUT | `/{productId}` |
| DELETE | `/{productId}` |

**Create body (shape)**
```json
{
  "name": "Blue T-Shirt",
  "sku": "TSH-001",
  "businessTypeId": 1,
  "categoryId": 5,
  "brandId": 2,
  "status": 1
}
```

### Master services — `service.*` → `/api/business-service`
Same pattern as products (`/list`, `/{id}`, `/search`, POST, PUT, bulk add).

---

## 6. Store offerings

### Store products — `store_product.*` → `/api/store-product`

| Method | Path |
|--------|------|
| GET | `/`, `/{productId}`, `/store/{storeId}`, `/business/{businessId}`, `/search/{term}`, `/search/{term}/{storeId}`, `/product-price-list/{productId}` |
| POST | `/save`, `/add-all` |

**Save body**
```json
{
  "storeId": 1,
  "businessProductId": 100,
  "name": "Blue T-Shirt",
  "status": 1
}
```

**List by store response:** `data: [ StoreOfferedProductDTO, ... ]`

### Store services — `store_service.*` → `/api/store-service`
Same pattern (`/store/{storeId}`, `/save`, …).

### Packages — `package.*` → `/api/store-offered-packages`
GET `/store/{storeId}`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### Pricing — `pricing.*` → `/store/...` (legacy prefix)

| Method | Path |
|--------|------|
| GET | `/store/lot/{lotId}/price`, `/store/lot/{lotId}/prices` |
| GET | `/store/service/{serviceId}/price`, `/prices` |
| POST | `/store/lot/price/add`, `/update` |
| POST | `/store/service/price/add`, `/update` |

**Add lot price**
```json
{
  "lotId": 55,
  "price": 19.99,
  "currency": "USD"
}
```

---

## 7. Employees — `user.*` → `/api/employees`

| Method | Path | Permission |
|--------|------|------------|
| GET | `/api/employees` | `user.read` |
| GET | `/api/employees/business/{businessId}` | `user.read` |
| GET | `/api/employees/store/{storeId}` | `user.read` |
| POST | `/api/employees` | `user.create` |
| PUT | `/api/employees/{id}` | `user.update` |
| DELETE | `/api/employees/{id}` | `user.delete` |

**Create body**
```json
{
  "businessId": 10,
  "storeId": 1,
  "name": "Alice Cashier",
  "email": "alice@acme.com",
  "roleId": 5,
  "phone": "+15550004444"
}
```

**Response `data`:** `EmployeeResponse` (`id`, `name`, `userId`, `storeId`, `businessId`, …).

---

## 8. Users / roles / permissions

### Users — `/api/users`
GET `/`, `/{id}`, `/email/{email}` · POST `/` (SYSTEM_ADMIN) · PUT `/{id}` · DELETE `/{id}` · POST `/{userId}/roles/{roleId}`

### Roles — `/api/roles`
CRUD + assign permissions (admin).

### Permissions — `/api/permissions`
GET `/`, POST `/`, DELETE `/{id}` — `user.permissions`

---

## 9. Customers — `customer.*` → `/api/customers`

| Method | Path |
|--------|------|
| GET | `/`, `/{id}`, by store/business variants |
| POST | `/` |
| PUT | `/{id}` |
| DELETE | `/{id}` |

**Create body**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "+15550005555",
  "storeId": 1,
  "businessId": 10
}
```

Scoped to accessible stores/businesses.

---

## 10. Orders — `order.*` → `/api/orders`

| Method | Path | Permission |
|--------|------|------------|
| GET | `/api/orders` | `order.read` |
| GET | `/api/orders/store/{storeId}` | `order.read` |
| GET | `/api/orders/customer/{customerId}` | `order.read` |
| GET | `/api/orders/{id}` | `order.read` |
| POST | `/api/orders/new` | `order.create` |
| PUT | `/api/orders/update/{id}` | `order.update` |
| POST | `/api/orders/{id}/cancel` | `order.update` |
| POST | `/api/orders/{id}/complete` | `order.update` |
| POST | `/api/orders/{id}/status?status=SHIPPED` | `order.update` |

### Sample — Place order

**Request**
```json
{
  "storeId": 1,
  "customerId": 20,
  "status": "CREATED",
  "notes": "POS walk-in",
  "items": [
    {
      "itemType": 1,
      "itemIId": 100,
      "itemName": "Blue T-Shirt",
      "quantity": 2,
      "price": 19.99
    }
  ]
}
```

**Response `data`**
```json
{
  "id": 501,
  "storeId": 1,
  "customerId": 20,
  "status": "BLOCKED",
  "totalAmount": 39.98,
  "orderDate": "2026-09-20T12:05:00",
  "items": [ /* ... */ ]
}
```

Foreign `storeId` → **403**.

### Order items — `/api/order-items`
CRUD with `order.*` / item permissions; scoped via parent order store.

---

## 11. Bookings / delivery / virtual products

### Bookings — `booking.*` → `/api/booking-details`
List, by store/customer/staff/date-range/status, create, update, status, delete.

**Create body (shape)**
```json
{
  "storeId": 1,
  "customerId": 20,
  "staffId": 3,
  "serviceId": 7,
  "startTime": "2026-09-21T10:00:00",
  "endTime": "2026-09-21T10:30:00",
  "status": "SCHEDULED"
}
```

### Delivery info — `/api/delivery-info` (often admin-gated in service)
### Virtual product details — `/api/virtual-product-details`

---

## 12. Inventory — `inventory.*`

### Store inventory — `/api/store-inventory`

| Method | Path |
|--------|------|
| GET | `/{storeId}` |
| GET | `/{storeId}/low-stock?threshold=5` |
| POST | `/{storeId}/{productId}/add?quantity=10` |
| POST | `/{storeId}/{productId}/sell?quantity=1` |

**Response (list):** stock rows for that store only.

### Product lots — `/api/product-lots`
GET by product, POST create lot, DELETE, etc.

**Create lot**
```json
{
  "productId": 100,
  "storeId": 1,
  "quantity": 50,
  "costPrice": 8.0,
  "expiryDate": "2027-01-01"
}
```

---

## 13. Invoices — `invoice.*` → `/api/invoices`

| Method | Path |
|--------|------|
| POST | `/generate` |
| GET | `/`, `/{id}`, `/order/{orderId}`, `/store/{storeId}` |
| GET | `/{id}/pdf` (binary PDF) |
| POST | `/{id}/email` |

**Generate body**
```json
{
  "orderId": 501
}
```

**Response `data`:** invoice header + line items. PDF returns `application/pdf` bytes.

---

## 14. Payments — `payment.*` → `/api/payments`

| Method | Path | Permission |
|--------|------|------------|
| POST | `/initiate` | `payment.create` |
| GET | `/status/{providerPaymentId}` | `payment.read` |
| POST | `/{providerPaymentId}/confirm` | `payment.create` |
| POST | `/{providerPaymentId}/refund` | `payment.refund` |
| POST | `/webhook/stripe` | public + signature |
| POST | `/webhook/razorpay` | public + signature |

### Sample — Initiate

**Request** (amount = **minor units**, e.g. ₹10.00 → `1000`)
```json
{
  "orderId": 501,
  "amount": 3998,
  "currency": "USD",
  "gateway": "STRIPE",
  "callbackUrl": "https://app.example.com/payment/callback"
}
```

**Response (Stripe)**
```json
{
  "paymentId": "cs_test_...",
  "providerOrderId": "cs_test_...",
  "redirectUrl": "https://checkout.stripe.com/c/pay/...",
  "gateway": "STRIPE",
  "clientKey": "pk_test_..."
}
```

**Response (Razorpay)** — open Checkout.js with `clientKey` + `providerOrderId`
```json
{
  "paymentId": "order_...",
  "providerOrderId": "order_...",
  "redirectUrl": null,
  "gateway": "RAZORPAY",
  "clientKey": "rzp_test_..."
}
```

Refund: `POST /api/payments/{providerPaymentId}/refund` with optional `{ "amount": 1000 }` (minor units) — calls Stripe/Razorpay APIs then updates local status. For Razorpay, refund after `payment.captured` webhook (needs `pay_...` id).

Unsigned webhook → **401**.

Also: `/api/payment-info` CRUD helpers; `/payment/callback` provider redirect.

---

## 15. Addresses — `address.*` → `/api/addresses`

GET `/`, `/{id}`, `/search?...` · POST `/` · PUT `/{id}` · DELETE `/{id}` (+ geo helpers in controller)

**Create body**
```json
{
  "line1": "1 Main St",
  "city": "Austin",
  "state": "TX",
  "postalCode": "78701",
  "country": "US",
  "storeId": 1
}
```

---

## 16. Files — `file.*` → `/api/files`

| Method | Path |
|--------|------|
| POST | `/upload` (multipart `file`) |
| POST | `/upload-multiple` |
| DELETE | `/?url=/uploads/<filename>` |

**Response**
```json
{ "success": true, "data": "/uploads/uuid.png", "message": "..." }
```

---

## Endpoint index (all ~219)

Machine-readable list: [`reports/api-endpoint-index.csv`](../reports/api-endpoint-index.csv)

Also: Postman collection [`postman/CraftLane_API_Collection.postman_collection.json`](../postman/CraftLane_API_Collection.postman_collection.json)

| Area | Approx. routes |
|------|----------------|
| addresses | 13 |
| stores | 12 |
| booking-details | 11 |
| auth + permissions demo/roles | ~25 |
| business-product / business-service | 20 |
| store-product / store-service | 18 |
| orders + order-items | 14 |
| invoices | 7 |
| payments (+ webhook/info) | ~12 |
| inventory + lots | 11 |
| customers | 8 |
| employees / users | 13 |
| brands / categories / business-type / business | ~26 |
| packages / pricing / files / me / nav | rest |

---

## Scope reminder

| Caller | Tenant APIs |
|--------|-------------|
| Store user | Only their store IDs |
| Business user | Only their business (+ its stores) |
| SYSTEM_ADMIN | All |
| Shared catalog | Permission only (not store-isolated) |

Use `GET /api/me/context` after login to drive UI store/business selection and headers.
