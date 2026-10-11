-- Enrich employee/membership history:
-- 1) optional reason on status transitions
-- 2) append-only employee_profile field change history
-- Current employee_profile / membership tables remain source of truth.

ALTER TABLE membership_status_history
    ADD COLUMN reason VARCHAR(512) NULL AFTER changed_by_user_id;

CREATE TABLE IF NOT EXISTS employee_profile_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    membership_id BIGINT NOT NULL,
    field_name VARCHAR(64) NOT NULL,
    old_value VARCHAR(512) NULL,
    new_value VARCHAR(512) NULL,
    changed_by_user_id BIGINT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_eph_membership (membership_id),
    KEY idx_eph_changed_at (changed_at),
    CONSTRAINT fk_eph_membership
        FOREIGN KEY (membership_id) REFERENCES membership (id) ON DELETE CASCADE
);

-- Seed current profile snapshot as initial rows (old_value NULL) so history is not empty.
INSERT INTO employee_profile_history (membership_id, field_name, old_value, new_value, changed_at)
SELECT p.membership_id, 'employeeNumber', NULL, p.employee_number, COALESCE(p.created_at, CURRENT_TIMESTAMP)
FROM employee_profile p
WHERE p.employee_number IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM employee_profile_history h
      WHERE h.membership_id = p.membership_id AND h.field_name = 'employeeNumber'
  );

INSERT INTO employee_profile_history (membership_id, field_name, old_value, new_value, changed_at)
SELECT p.membership_id, 'jobTitle', NULL, p.job_title, COALESCE(p.created_at, CURRENT_TIMESTAMP)
FROM employee_profile p
WHERE p.job_title IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM employee_profile_history h
      WHERE h.membership_id = p.membership_id AND h.field_name = 'jobTitle'
  );

INSERT INTO employee_profile_history (membership_id, field_name, old_value, new_value, changed_at)
SELECT p.membership_id, 'hireDate', NULL, p.hire_date, COALESCE(p.created_at, CURRENT_TIMESTAMP)
FROM employee_profile p
WHERE p.hire_date IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM employee_profile_history h
      WHERE h.membership_id = p.membership_id AND h.field_name = 'hireDate'
  );

INSERT INTO employee_profile_history (membership_id, field_name, old_value, new_value, changed_at)
SELECT p.membership_id, 'leaveDate', NULL, p.leave_date, COALESCE(p.created_at, CURRENT_TIMESTAMP)
FROM employee_profile p
WHERE p.leave_date IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM employee_profile_history h
      WHERE h.membership_id = p.membership_id AND h.field_name = 'leaveDate'
  );
