-- Per-tenant config for accepting lower-than-entitled client discount claims on place-order.
-- NULL = not configured (fall through to next level / global default true).

ALTER TABLE business
    ADD COLUMN accept_lower_client_discount BOOLEAN NULL;

ALTER TABLE store
    ADD COLUMN accept_lower_client_discount BOOLEAN NULL;
