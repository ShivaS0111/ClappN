-- Membership (User ↔ Business) with roles & store scopes; EmployeeProfile for HR fields.
-- Migrates data from legacy employee (+ optional user_roles for non-platform roles stays on user for SYSTEM_ADMIN).

CREATE TABLE IF NOT EXISTS membership (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_membership_user_business (user_id, business_id),
    KEY idx_membership_user (user_id),
    KEY idx_membership_business (business_id),
    KEY idx_membership_status (status)
);

CREATE TABLE IF NOT EXISTS membership_role (
    membership_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (membership_id, role_id),
    KEY idx_membership_role_role (role_id),
    CONSTRAINT fk_membership_role_membership FOREIGN KEY (membership_id) REFERENCES membership (id) ON DELETE CASCADE,
    CONSTRAINT fk_membership_role_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS membership_store_scope (
    membership_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    PRIMARY KEY (membership_id, store_id),
    KEY idx_membership_store_scope_store (store_id),
    CONSTRAINT fk_membership_store_scope_membership FOREIGN KEY (membership_id) REFERENCES membership (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employee_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    membership_id BIGINT NOT NULL,
    employee_number VARCHAR(64) NULL,
    hire_date VARCHAR(64) NULL,
    job_title VARCHAR(128) NULL,
    name VARCHAR(255) NULL,
    first_name VARCHAR(128) NULL,
    last_name VARCHAR(128) NULL,
    sur_name VARCHAR(128) NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(64) NULL,
    leave_date VARCHAR(64) NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_employee_profile_membership (membership_id),
    KEY idx_employee_profile_user (user_id),
    CONSTRAINT fk_employee_profile_membership FOREIGN KEY (membership_id) REFERENCES membership (id) ON DELETE CASCADE
);

-- Migrate legacy employee rows → membership + profile (+ role + store scope)
-- Resolve business_id from employee.business_id or store.business_id when missing.
INSERT INTO membership (user_id, business_id, status, created_at, updated_at)
SELECT DISTINCT
    e.user_id,
    COALESCE(e.business_id, s.business_id) AS business_id,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM employee e
LEFT JOIN store s ON s.id = e.store_id
WHERE e.user_id IS NOT NULL
  AND COALESCE(e.business_id, s.business_id) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM membership m
      WHERE m.user_id = e.user_id
        AND m.business_id = COALESCE(e.business_id, s.business_id)
  );

INSERT INTO membership_role (membership_id, role_id)
SELECT DISTINCT m.id, e.role_id
FROM employee e
LEFT JOIN store s ON s.id = e.store_id
INNER JOIN membership m
    ON m.user_id = e.user_id
   AND m.business_id = COALESCE(e.business_id, s.business_id)
WHERE e.role_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM membership_role mr
      WHERE mr.membership_id = m.id AND mr.role_id = e.role_id
  );

INSERT INTO membership_store_scope (membership_id, store_id)
SELECT DISTINCT m.id, e.store_id
FROM employee e
LEFT JOIN store s ON s.id = e.store_id
INNER JOIN membership m
    ON m.user_id = e.user_id
   AND m.business_id = COALESCE(e.business_id, s.business_id)
WHERE e.store_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM membership_store_scope mss
      WHERE mss.membership_id = m.id AND mss.store_id = e.store_id
  );

INSERT INTO employee_profile (
    user_id, membership_id, employee_number, hire_date, job_title,
    name, first_name, last_name, sur_name, email, phone, leave_date
)
SELECT
    e.user_id,
    m.id,
    e.employee_code,
    e.join_date,
    NULL,
    e.name,
    e.first_name,
    e.last_name,
    e.sur_name,
    e.email,
    e.phone,
    e.leave_date
FROM employee e
LEFT JOIN store s ON s.id = e.store_id
INNER JOIN membership m
    ON m.user_id = e.user_id
   AND m.business_id = COALESCE(e.business_id, s.business_id)
WHERE e.user_id IS NOT NULL
  AND COALESCE(e.business_id, s.business_id) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM employee_profile ep WHERE ep.membership_id = m.id
  );
