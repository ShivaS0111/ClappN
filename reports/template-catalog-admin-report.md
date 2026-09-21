# ClappN Template / Catalog / Admin Report

Generated: 2026-08-10T12:39:50.151025400Z

Category: C Shared catalog authorization

## Notes

- SHARED_CATALOG: templates are business-type scoped, not business-id scoped
- STORE_MANAGER brand.read=true → ALLOW on /api/brands is correct
- Delivery-info and virtual-product-details list endpoints require SYSTEM_ADMIN (service-layer AccessDeniedException), not merely order.read/product.read

## Summary

| Metric | Value |
|---|---|
| Pass | 10 |
| Fail | 0 |
| Warn | 0 |
| Skip | 0 |
| Total | 10 |

## Failures

_None_
