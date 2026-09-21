# ClappN Thorough System Feature Report

Generated: 2026-08-10T10:51:46.926676700Z

## Summary

| Metric | Value |
|---|---|
| Total probes | 241 |
| Pass | 168 |
| Fail | 37 |
| Warn | 36 |
| Skip | 0 |
| Pass rate | 69.7% |
| Logins | 35/35 |

## Seed

- Business ID: 252
- Store A/B: 52 / 53
- Product/Service templates: 6 / 102
- Memberships: 32
- Password (all thorough.* users): `Test@12345`

## Failures

| Role | Action | HTTP | Detail |
|---|---|---|---|
| SYSTEM_ADMIN | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| BUSINESS_OWNER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| BUSINESS_ADMIN | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| BUSINESS_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| STORE_OWNER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| STORE_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| ASSISTANT_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| SHIFT_SUPERVISOR | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| SALES_ASSOCIATE | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| CASHIER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| INVENTORY_STAFF | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| CUSTOMER_SERVICE_REP | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| STOCK_KEEPER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| VISUAL_MERCHANDISER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| SECURITY_STAFF | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| CLEANING_STAFF | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| INVENTORY_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| SALES_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| CUSTOMER_SERVICE | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| FINANCE_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| MARKETING_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| SECURITY_OFFICER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| MAINTENANCE_STAFF | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| CUSTOMER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| GUEST | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| VENDOR_ADMIN | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| VENDOR_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| APPROVAL_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| CONTENT_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| ANALYTICS_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| SUPPORT_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| WAREHOUSE_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| LOGISTICS_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| QUALITY_ASSURANCE | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| RETURNS_MANAGER | GET /api/me/context | 401 | {"data":null,"success":false,"message":"Unauthorized: authentication required","status":401} |
| BUSINESS_ADMIN | GET /api/business [business.read] expect=DENY | 200 | {"success":true,"message":"Businesses retrieved successfully","statusCode":200,"timestamp":"2026-08-10T16:21:43.370576","data":{"content":[{"id":252,"businessName":"Thorough Test B… |
| CASHIER | POST /api/orders | 500 | {"success":false,"message":"Internal server error","statusCode":500,"timestamp":"2026-08-10T16:21:46.8865028"} |
