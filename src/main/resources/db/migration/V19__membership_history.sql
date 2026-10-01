-- Append-only history for membership status changes and role/store-scope assignment
-- periods. Existing tables (membership, membership_role, membership_store_scope,
-- employee_profile) remain the "current state" source of truth and are not modified.
-- These history tables are purely additive and do not affect existing functionality.

CREATE TABLE IF NOT EXISTS membership_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    membership_id BIGINT NOT NULL,
    old_status VARCHAR(32) NULL,
    new_status VARCHAR(32) NOT NULL,
    changed_by_user_id BIGINT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_msh_membership (membership_id),
    KEY idx_msh_changed_at (changed_at),
    CONSTRAINT fk_msh_membership
        FOREIGN KEY (membership_id) REFERENCES membership (id) ON DELETE CASCADE
);

-- One row per (role, scope) assignment period. scope='BUSINESS' => store_id NULL
-- (role applies business-wide); scope='STORE' => store_id set (role applies at that
-- store). valid_to NULL means the assignment is currently in effect. user_id/business_id
-- are point-in-time snapshots (not a live join) so history stays accurate even if a
-- membership's business_id is later changed.
CREATE TABLE IF NOT EXISTS membership_role_assignment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scope VARCHAR(16) NOT NULL,
    user_id BIGINT NOT NULL,
    membership_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    store_id BIGINT NULL,
    role_id BIGINT NULL,
    role_name VARCHAR(255) NULL,
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP NULL,
    changed_by_user_id BIGINT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_mrah_membership (membership_id),
    KEY idx_mrah_user (user_id),
    KEY idx_mrah_business (business_id),
    KEY idx_mrah_open (membership_id, store_id, valid_to),
    CONSTRAINT fk_mrah_membership
        FOREIGN KEY (membership_id) REFERENCES membership (id) ON DELETE CASCADE
);

-- Backfill: seed an initial open history row per existing membership/role/store
-- combination so the timeline starts from current state instead of appearing empty.
INSERT INTO membership_status_history (membership_id, old_status, new_status, changed_at)
SELECT m.id, NULL, m.status, m.created_at
FROM membership m
WHERE NOT EXISTS (SELECT 1 FROM membership_status_history h WHERE h.membership_id = m.id);

-- Business-level roles: memberships with no store scope rows.
INSERT INTO membership_role_assignment_history
    (scope, user_id, membership_id, business_id, store_id, role_id, role_name, valid_from, changed_at)
SELECT 'BUSINESS', m.user_id, m.id, m.business_id, NULL, mr.role_id, r.name, m.created_at, m.created_at
FROM membership m
INNER JOIN membership_role mr ON mr.membership_id = m.id
LEFT JOIN role r ON r.id = mr.role_id
WHERE NOT EXISTS (SELECT 1 FROM membership_store_scope mss WHERE mss.membership_id = m.id)
  AND NOT EXISTS (
      SELECT 1 FROM membership_role_assignment_history h
      WHERE h.membership_id = m.id AND h.role_id = mr.role_id AND h.store_id IS NULL
  );

-- Store-level roles: one row per (role, store) combination currently in scope.
INSERT INTO membership_role_assignment_history
    (scope, user_id, membership_id, business_id, store_id, role_id, role_name, valid_from, changed_at)
SELECT 'STORE', m.user_id, m.id, m.business_id, mss.store_id, mr.role_id, r.name, m.created_at, m.created_at
FROM membership m
INNER JOIN membership_store_scope mss ON mss.membership_id = m.id
INNER JOIN membership_role mr ON mr.membership_id = m.id
LEFT JOIN role r ON r.id = mr.role_id
WHERE NOT EXISTS (
    SELECT 1 FROM membership_role_assignment_history h
    WHERE h.membership_id = m.id AND h.role_id = mr.role_id AND h.store_id = mss.store_id
);
