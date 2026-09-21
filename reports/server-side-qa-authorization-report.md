# SERVER-SIDE QA & AUTHORIZATION REPORT

Generated: 2026-09-20T13:40:27.803523Z

**Overall status: PASS**

## 1. Executive Summary

Combined thorough authorization suite aggregating slice reports under `reports/`.

- Permission matrix: pass=3360, fail=0, skip=0
- Tenant isolation: pass=30, fail=0, skip=0
- Authentication: pass=9, fail=0, skip=1
- CRUD / IDOR / Products: pass=16, fail=0, skip=0
- Template / Catalog / Admin: pass=10, fail=0, skip=0
- Indirect integrity: pass=3, fail=0, skip=0
- Overall: **PASS**

## 2. Test Environment

- Profile: `thorough`
- Stack: Spring Boot + MockMvc + JWT login
- Password (seed users): `Test@12345`

## 3. Test Dataset

```text
Business A (Thorough Test Business) id=252
  Store A1 (Thorough Store Alpha) id=454
  Store A2 (Thorough Store Beta) id=53
  Store A3 (Thorough Store Gamma) id=353
Business B (Thorough Isolation Business B) id=302
  Store B1 (Thorough Store B1) id=354
  Store B2 (Thorough Store B2) id=355
```

Special users:

| Key | Email | User id |
|---|---|---|
| `scope.business_a` | thorough.scope.business_a@clapp.test | 455 |
| `scope.store_a1` | thorough.scope.store_a1@clapp.test | 456 |
| `scope.store_a1_a2` | thorough.scope.store_a1_a2@clapp.test | 457 |
| `scope.business_b` | thorough.scope.business_b@clapp.test | 458 |
| `scope.none` | thorough.scope.none@clapp.test | 459 |
| `scope.inactive` | thorough.scope.inactive@clapp.test | 460 |
| `auth.disabled` | thorough.auth.disabled@clapp.test | 505 |

## 4. User / Role / Permission Matrix

From FullApiRoleAuthzIT: pass=3360, fail=0, warn=0. See `reports/full-api-role-authz-report.md`.

## 5. Membership / Employee Scope Matrix

| Special user | Role | Business | Store scopes |
|---|---|---|---|
| scope.business_a | BUSINESS_OWNER | A | empty (= all A stores) |
| scope.store_a1 | STORE_MANAGER | A | A1 only |
| scope.store_a1_a2 | STORE_MANAGER | A | A1+A2 |
| scope.business_b | BUSINESS_OWNER | B | empty (= all B stores) |
| scope.none | (none) | — | no membership |
| scope.inactive | BUSINESS_OWNER | A | INACTIVE membership |
| auth.disabled | (none) | — | enabled=false |

## 6. Endpoint Coverage

Permission matrix (FullApiRoleAuthzIT), tenant isolation, auth full, CRUD/IDOR/products, template/catalog/admin, and indirect integrity slices.

## 7. Authentication Results

**PASS** — pass=9, fail=0, skip=1, warn=0

| Verdict | User | Probe | HTTP |
|---|---|---|---|
| PASS | valid-user | Valid login → 200 + token | 200 |
| PASS | valid-user | Invalid password → 401 | 401 |
| PASS | unknown | Unknown user → 401 | 401 |
| PASS | anonymous | No Authorization → 401 | 401 |
| PASS | garbage | Garbage Bearer → 401 | 401 |
| PASS | auth.disabled | Disabled user login → 401/403 | 401 |
| PASS | valid-user | Logout with refreshToken | 200 |
| PASS | anonymous | Logout blank refreshToken → 4xx | 400 |
| PASS | valid-user | GET /api/me/context after login → 200 | 200 |
| SKIP |  | Password change | 0 |
## 8. Authorization Results

**PASS** (executed permission matrix) — authzOk=true.

## 9. Tenant Isolation Results

**PASS** — pass=30, fail=0, skip=0, warn=0

| Verdict | User | Probe | HTTP |
|---|---|---|---|
| PASS | scope.store_a1 | GET /api/stores/{A1} | 200 |
| PASS | scope.store_a1 | GET /api/stores/{A3} deny | 403 |
| PASS | scope.store_a1 | GET /api/stores/{B1} deny | 403 |
| PASS | scope.store_a1 | GET /api/stores/list scoped | 200 |
| PASS | scope.store_a1 | GET /api/business/{A} no business.read | 403 |
| PASS | scope.store_a1 | GET /api/business/{B} deny | 403 |
| PASS | scope.store_a1 | GET store-product A1 allow | 200 |
| PASS | scope.store_a1 | GET store-product B1 deny | 403 |
| PASS | scope.store_a1 | GET store-product/store/A1 | 200 |
| PASS | scope.store_a1 | GET store-product/store/B1 deny | 403 |
| PASS | scope.store_a1 | GET customer A allow | 200 |
| PASS | scope.store_a1 | GET customer B deny | 403 |
| PASS | scope.business_a | GET store A1 (biz-wide) | 200 |
| PASS | scope.business_a | GET store A2 (biz-wide) | 200 |
| PASS | scope.business_a | GET store A3 (biz-wide) | 200 |
| PASS | scope.business_a | GET store B1 deny (biz-wide) | 403 |
| PASS | scope.business_a | GET /api/stores/list only A | 200 |
| PASS | scope.business_a | GET business A allow | 200 |
| PASS | scope.business_a | GET business B deny | 403 |
| PASS | scope.store_a1_a2 | GET A1 (multi-store) | 200 |
| PASS | scope.store_a1_a2 | GET A2 (multi-store) | 200 |
| PASS | scope.store_a1_a2 | GET A3 deny (multi-store) | 403 |
| PASS | scope.store_a1_a2 | list A1+A2 only | 200 |
| PASS | scope.none | none → protected store | 403 |
| PASS | scope.none | none → protected business | 403 |
| PASS | scope.inactive | inactive → protected store | 403 |
| PASS | scope.inactive | inactive → protected business | 403 |
| PASS | scope.store_a1 | X-Store-Id=A1 ok | 200 |
| PASS | scope.store_a1 | X-Store-Id=A3 → 403 | 403 |
| PASS | scope.store_a1 | X-Store-Id=B1 → 403 | 403 |
## 10. Business-Level Access Results

Covered by TenantIsolationIT (business_a / business_b special users). See section 9.

## 11. Store-Level Access Results

Covered by TenantIsolationIT (store_a1 / store_a1_a2 / X-Store-Id). See section 9.

## 12. CRUD Results

| Verdict | User | Probe | HTTP |
|---|---|---|---|
| PASS | CASHIER | CASHIER DELETE A1 → 403 (no store.delete) | 403 |
| PASS | scope.business_a | BUSINESS_OWNER list stores A | 200 |

## 13. Product Results

| Verdict | User | Probe | HTTP |
|---|---|---|---|
| PASS | scope.store_a1 | GET store-product/store/A1 allow | 200 |
| PASS | scope.store_a1 | GET store-product/store/B1 deny | 403 |
| PASS | scope.store_a1 | POST empty store-product → 4xx not 500 | 403 |

## 14. Service Results

**PENDING** — dedicated service-template mutation suite not in this run.

## 15. Template / Catalog Results

| Verdict | User | Probe | HTTP |
|---|---|---|---|
| PASS | SYSTEM_ADMIN | SYSTEM_ADMIN GET /api/brands | 200 |
| PASS | SYSTEM_ADMIN | SYSTEM_ADMIN GET /api/categories/list | 200 |
| PASS | SYSTEM_ADMIN | SYSTEM_ADMIN GET /api/business-type/list | 200 |
| PASS | scope.store_a1 | store_a1 GET /api/brands (brand.read=true) | 200 |
| PASS | scope.store_a1 | GET /api/business-product/list (product.read) | 200 |

Notes:
- SHARED_CATALOG: templates are business-type scoped, not business-id scoped
- STORE_MANAGER brand.read=true → ALLOW on /api/brands is correct
- Delivery-info and virtual-product-details list endpoints require SYSTEM_ADMIN (service-layer AccessDeniedException), not merely order.read/product.read

## 16. Admin-Only API Results

| Verdict | User | Probe | HTTP |
|---|---|---|---|
| PASS | SYSTEM_ADMIN | SYSTEM_ADMIN GET /api/delivery-info | 200 |
| PASS | scope.store_a1 | store_a1 GET /api/delivery-info → 403 | 403 |
| PASS | scope.store_a1 | store_a1 GET /api/virtual-product-details → 403 | 403 |
| PASS | SYSTEM_ADMIN | SYSTEM_ADMIN GET /api/virtual-product-details | 200 |

## 17. IDOR Results

| Verdict | User | Probe | HTTP |
|---|---|---|---|
| PASS | scope.store_a1 | GET store A1 allow | 200 |
| PASS | scope.store_a1 | GET store A3 deny | 403 |
| PASS | scope.store_a1 | GET store B1 deny | 403 |
| PASS | scope.store_a1 | GET customer A allow | 200 |
| PASS | scope.store_a1 | GET customer B deny | 403 |
| PASS | scope.store_a1 | PUT foreign store A3 → 403 | 403 |
| PASS | scope.store_a1 | DELETE foreign store B1 → 403 | 403 |

## 18. Indirect Authorization Results

**PASS** — pass=3, fail=0, skip=0, warn=0

| Verdict | User | Probe | HTTP |
|---|---|---|---|
| PASS | scope.store_a1 | POST store-product storeId=B1 as store_a1 → DENY | 403 |
| PASS | scope.store_a1 | POST /api/orders/new storeId=B1 → DENY | 403 |
| PASS | scope.store_a1 | POST order customerB + storeA1 → deny/validation | 400 |
## 19. Validation Results

Exercised via empty POST bodies and cross-tenant order payloads (expect 4xx not 500). See CRUD / Indirect reports.

## 20. Data Integrity Results

| Verdict | User | Probe | HTTP |
|---|---|---|---|
| PASS | scope.business_a | A3 name unchanged after foreign PUT | 200 |
| PASS | scope.business_b | B1 still readable after denied DELETE | 200 |
| PASS | scope.store_a1 | PUT B1 as store_a1 → 403 | 403 |
| PASS | scope.business_b | B1 storeName not HACKED after denied PUT | 200 |

Also see IndirectIntegrityIT mutation denies.

## 21. Bugs / Findings

_No failures in executed slices._

## 22. Security Findings

### Finding categories

| Code | Name | Question |
|---|---|---|
| A | Tenant security | Prevented from other business/store tenant data? |
| B | Permission security | Has required permission for the operation? |
| C | Shared catalog authorization | Allowed to use global / business-type catalog items? |

## 23. Regression Findings

**PENDING** — compare against prior thorough reports when available.

## 24. Coverage Statistics

| Slice | Status | Pass | Fail |
|---|---|---|---|
| Permission matrix (FullApiRoleAuthzIT) | EXECUTED/PASS | 3360 | 0 |
| Tenant + business/store scope (TenantIsolationIT) | EXECUTED/PASS | 30 | 0 |
| Authentication (AuthFullIT) | EXECUTED/PASS | 9 | 0 |
| CRUD / IDOR / Products (CrudIdorProductsIT) | EXECUTED/PASS | 16 | 0 |
| Template / Catalog / Admin (TemplateCatalogAdminIT) | EXECUTED/PASS | 10 | 0 |
| Indirect integrity (IndirectIntegrityIT) | EXECUTED/PASS | 3 | 0 |

## 25. Recommendations

1. Keep tagged thorough ITs green in CI.
2. Classify findings A/B/C before treating shared catalog access as a tenant leak.
3. Extend service-template mutation coverage when ready.

## 26. Final PASS / FAIL Assessment

| Executed slice | Result |
|---|---|
| Permission matrix | PASS |
| Tenant isolation | PASS |
| Authentication | PASS |
| CRUD / IDOR / Products | PASS |
| Template / Catalog / Admin | PASS |
| Indirect integrity | PASS |
| **Overall** | **PASS** |
