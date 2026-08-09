# Flyway migrations (canonical)
#
# Location: src/main/resources/db/migration/
# Naming:   V{version}__description.sql
#
# Current chain: V1 baseline → V2 placeholder → V3…V14 incremental DDL
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
