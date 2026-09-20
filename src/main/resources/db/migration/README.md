# Flyway migrations (canonical)
#
# Location: src/main/resources/db/migration/
# Naming:   V{version}__description.sql
#
# Current chain: V1…V14 → V15 membership + employee_profile → V16 drop legacy employee
#
# V15–V16 model
# - membership: User ↔ Business with status, roles (membership_role), store scopes
# - employee_profile: HR fields (employee_number, hire_date, job_title, …) per membership
# - V15 migrates legacy `employee` rows; V16 drops the `employee` table
# - SYSTEM_ADMIN stays on user_roles
#
# Existing databases
# - If flyway_schema_history already has versions but checksums fail after
#   restoring scripts: run `mvnw flyway:repair` once, then restart.
# - Do not delete applied migrations; only add new Vn scripts.
#
# New / empty databases
# - baseline-on-migrate + baseline-version=1 marks V1 as baseline, then
#   applies V2+. Core tables still expected from app bootstrap/dumps for
#   early history; from V5 onward scripts create/alter tables safely.
#
# Archive copies under ClappN/db/ are non-canonical — edit classpath files only.
