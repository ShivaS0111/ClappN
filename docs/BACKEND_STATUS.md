# ClappN Backend — Current Status

**Analyzed from live source:** 28 Sep 2026 (updated same day — Gaps 1–4 closed)  
**Repo path:** `ClappN/`  
**Artifact:** `biz.craftline:server:0.0.1-SNAPSHOT`  
**Runtime note:** Server was **not running** at analysis time (default port `9090`).

---

## Stack

| Item | Value |
|------|--------|
| Framework | Spring Boot **3.1.4** |
| Language | Java **17** |
| Persistence | Spring Data JPA + MySQL |
| Migrations | Flyway (`classpath:db/migration`, V1–V17) |
| Security | Spring Security + JWT + method security |
| Observability | Actuator (health/info/metrics/prometheus), Micrometer, structured logging |
| Mail | `MailService` — `log` (default) or SMTP |
| PDF | OpenPDF (invoices) |
| Payments | Stripe Java SDK + Razorpay REST |
| Default port / profile | **9090** / `dev` |

---

## Security (live)

`SecurityConfig` is **not** open:

- Public: login/register/refresh/logout/forgot/reset, payment webhooks, payment callback GET, actuator health/info
- Everything else: **authenticated**
- Filters: `JwtAuthenticationFilter` → `UserScopeFilter` (DB permissions + store/business scope)
- Enforcement: `@RequirePermission` + `SecurityContextService.validateStoreAccess` / `validateBusinessAccess`
- Prod profile: JWT secret required, swagger off, `ddl-auto=validate`, demo seed off

**Auth APIs:** `/api/auth/*` — register, login, refresh-token **and** `/refresh`, logout, forgot/reset/change password, validate, revoke-all-refresh-tokens.  
**Context:** `GET /api/me/context` for FE workspace scope.

---

## Feature modules (from live code)

| Module | Controllers / surface | Status | ~% |
|--------|----------------------|--------|----|
| **usermanagement** | Auth, User, Role, Permission, MeContext, Navigation | Strong — JWT, refresh DB, mail on forgot-password, RBAC seed | **92%** |
| **membership** | `/api/memberships` list/get/by-business/by-user + create/update/activate/deactivate | Tenancy access API (roles + store scopes); HR stays on employees | **95%** |
| **employeemanagement** | `/api/employees` CRUD + GET by id + deactivate | Membership-based; store scope replace; soft delete | **92%** |
| **businessstore** | Business, Store, store-product/service, packages, pricing, files | CRUD + metrics + packages; hardened assign + PUT/DELETE + **`/assign` multi-store** | **95%** |
| **businesstype** | Types, categories, brands, business-product/service | Master catalog with **tenant `businessId`** + scoped CRUD + `listByBusiness` | **92%** |
| **customermanagement** | `/api/customers` | Scoped CRUD; businessId list; require store/business on create | **92%** |
| **addressmanagement** | `/api/addresses` | Scoped CRUD; update maps geo FKs | **90%** |
| **inventorymanagement** | Store inventory, lots, **low-stock** API | Lots + adjust; lot-tx scoped; performedBy from auth | **90%** |
| **ordermanagement** | Orders, items, bookings, delivery, virtual products | Lifecycle + enum status; delivery/virtual admin-only standalone writes | **90%** |
| **invoicemanagement** | Generate (idempotent), void, list, **PDF**, **email** | Usable; tax engine light | **88%** |
| **paymentmanagement** | Initiate (amount vs order), by-order, status, **confirm**, **refund**, webhooks | Code complete; needs real keys + staging smoke | **90%** code / **~60%** ops |
| **couponmanagement** | Package folders only — **no Java sources** | Scaffold only; RBAC names may exist | **5%** |

### Catalog model (important)

```
BusinessType  →  business_product / business_service
                 (+ optional businessId: null=template, set=tenant-owned)
Business      →  owns catalog items (businessId)
Store         →  store_offered_* assign (businessId + storeId + catalog id)
```

**Gap 1 (done):** Assign validation + `PUT`/`DELETE` offerings.  
**Gap 2 (done):** Flyway `V17` + `businessId` on masters; non-admin create requires business; list filtered; assign rejects foreign-business catalog.  
**Gap 3 (done):** `POST .../assign` (`allStores` or `storeIds`).  
**Gap 4 (done):** `POST .../unassign` + per-business (case-insensitive) catalog name uniqueness.

### Pricing model

```
business_product/service.amount  →  optional default (nullable, not required)
store_item_price                 →  store override (wins when present)
store list APIs                  →  override else business default else no price
```

Flyway **`V18`**: `amount` columns nullable.

---

## Flyway

Classpath migrations present:

`V1`–`V16` as before · **`V17` catalog `business_id` ownership** · **`V18` optional business default `amount`**.

Legacy/helper SQL also under `ClappN/db/`. Prod uses validate-on-migrate; clean envs should rely on classpath V1–V17.

---

## Tests

~**80+** Java test files under `src/test` including:

- Security/scope unit tests (`UserScopeFilter`, `SecurityContextService`, permission aspect/matrix)
- Module service/controller unit tests (orders, payments, inventory, business, auth, …)
- Thorough/IT-style suites under `thorough/` + staging smoke helpers
- Reports generated historically under `ClappN/reports/`

---

## Production readiness

| Area | State |
|------|--------|
| Authz / tenant scope | **Strong** for core paths |
| Observability | Actuator + request logging present |
| Payments | **Blocked on real secrets** + webhook verify in prod (`PaymentStartupValidator`) |
| Mail | Log mode by default; SMTP when configured |
| Catalog multi-tenant | **Supported** (`businessId` on masters + scoped assign) |
| Coupons / reports / approvals / vendor | **Not backend-complete** |
| Local runtime | Requires JDK 17 + MySQL; not running at last check |

**Overall backend (existing modules, excl. keys/coupons/tax):** ~**98–99%**  
**Production readiness (ops + gaps):** ~**70–75%**  
**Full platform (incl. marketing modules):** ~**72–78%**

---

## Top gaps (priority)

1. ~~Harden assign + offering update/delete~~ **Done**
2. ~~Business-owned catalog~~ **Done**
3. ~~Multi-store assign / unassign~~ **Done**
4. ~~Per-business catalog name uniqueness~~ **Done**
5. **Payment staging smoke** with real Stripe/Razorpay keys *(excluded from 99% bar)*
6. **couponmanagement** — empty *(excluded)*
7. Tax engine depth *(excluded)*
8. SMTP + rate-limit / token revoke (nice-to-have)
9. ~~Membership REST API~~ **Done** (`/api/memberships`)

---

## Key endpoint map (non-exhaustive)

| Area | Base |
|------|------|
| Auth | `/api/auth` |
| Me / scope | `/api/me/context` |
| Business / stores | `/api/business`, `/api/stores` |
| Master product/service | `/api/business-product`, `/api/business-service` (+ `/listByBusiness/{id}`) |
| Store offerings | `/api/store-product`, `/api/store-service` (+ `/assign`, `/unassign`, PUT/DELETE `/{id}`), packages |
| Inventory | `/api/store-inventory`, lots APIs |
| Orders / bookings | `/api/orders` (+ item/booking/delivery controllers) |
| Invoices | `/api/invoices`, `/generate`, `/{id}/void`, `/{id}/pdf`, `/{id}/email` |
| Payments | `/api/payments/initiate`, `/order/{orderId}`, `/status/{id}`, `/{id}/confirm`, `/{id}/refund`, `/api/payments/webhook/{stripe\|razorpay}` |
| Employees | `/api/employees`, `/{id}`, `/{id}/deactivate` |
| Memberships | `/api/memberships`, `/business/{id}`, `/user/{id}`, `/{id}/activate\|deactivate` |
| Actuator | `/actuator/health`, `/actuator/info` (metrics auth’d) |

---

## Related docs

- [CREATION_FLOW_DIAGRAMS.md](./CREATION_FLOW_DIAGRAMS.md) — create flows
- [API_CATALOG_SAMPLES.md](./API_CATALOG_SAMPLES.md) — sample bodies
- Root [PROJECT_ASSESSMENT_REPORT.md](../../PROJECT_ASSESSMENT_REPORT.md) — project-wide snapshot (updated same date)
