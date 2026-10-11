-- Promotions MVP: LINE/CART percent+fixed, optional coupon code, business/store/user/catalog targets, usage.

CREATE TABLE IF NOT EXISTS promotion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(64) NULL,
    benefit_type VARCHAR(32) NOT NULL,
    application_level VARCHAR(16) NOT NULL,
    benefit_value DECIMAL(19,4) NOT NULL,
    business_id BIGINT NOT NULL,
    store_id BIGINT NULL,
    min_cart_amount DECIMAL(19,4) NULL,
    max_discount_amount DECIMAL(19,4) NULL,
    max_global_redemptions INT NULL,
    max_per_user INT NULL,
    start_at TIMESTAMP NULL,
    end_at TIMESTAMP NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    priority INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_promo_business (business_id),
    KEY idx_promo_store (store_id),
    KEY idx_promo_status (status),
    KEY idx_promo_code (code),
    UNIQUE KEY uk_promo_business_code (business_id, code)
);

CREATE TABLE IF NOT EXISTS promotion_target (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_id BIGINT NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    KEY idx_pt_promo (promotion_id),
    KEY idx_pt_type_id (target_type, target_id),
    CONSTRAINT fk_pt_promotion FOREIGN KEY (promotion_id) REFERENCES promotion (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS promotion_redemption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    store_id BIGINT NULL,
    discount_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    redeemed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_pr_promo (promotion_id),
    KEY idx_pr_order (order_id),
    KEY idx_pr_user (user_id),
    CONSTRAINT fk_pr_promotion FOREIGN KEY (promotion_id) REFERENCES promotion (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS promotion_usage_stats (
    promotion_id BIGINT NOT NULL PRIMARY KEY,
    redemption_count BIGINT NOT NULL DEFAULT 0,
    total_discount_given DECIMAL(19,4) NOT NULL DEFAULT 0,
    CONSTRAINT fk_pus_promotion FOREIGN KEY (promotion_id) REFERENCES promotion (id) ON DELETE CASCADE
);
