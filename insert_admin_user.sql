-- Admin 사용자 INSERT SQL
-- MySQL Workbench에서 실행하세요

INSERT INTO users (
    login_id, password, email, name, nickname, phone_number, status_message,
    country, birth_date, height, weight, profile_image_url,
    is_account_public, is_birth_public, is_height_public, is_weight_public,
    status, created_at, updated_at, password_updated_at
) VALUES (
    'admin', 'admin123', 'admin@chozy.com', 'Admin', 'admin', NULL, '관리자 계정입니다',
    'Korea', NULL, 0.0, 0.0, NULL,
    1, 0, 0, 0,
    'ACTIVE', NOW(), NOW(), NOW()
);
