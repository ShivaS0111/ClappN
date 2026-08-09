-- Packages + collection tables used by StoreOfferedPackageEntity (prod ddl-auto=validate)

CREATE TABLE IF NOT EXISTS store_offered_package (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1000) NULL,
  store_id BIGINT NOT NULL,
  status INT NULL,
  price DOUBLE NULL,
  available BOOLEAN NULL,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_sop_store (store_id)
);

CREATE TABLE IF NOT EXISTS store_offered_package_products (
  package_id BIGINT NOT NULL,
  store_product_id BIGINT NOT NULL,
  PRIMARY KEY (package_id, store_product_id),
  CONSTRAINT fk_sopp_package FOREIGN KEY (package_id) REFERENCES store_offered_package(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS store_offered_package_services (
  package_id BIGINT NOT NULL,
  store_service_id BIGINT NOT NULL,
  PRIMARY KEY (package_id, store_service_id),
  CONSTRAINT fk_sops_package FOREIGN KEY (package_id) REFERENCES store_offered_package(id) ON DELETE CASCADE
);
