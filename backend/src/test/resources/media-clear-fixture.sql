UPDATE cms_content SET cover_url = '/uploads/content-cover.png' WHERE id = 1001;
UPDATE exhibition SET cover_url = '/uploads/exhibition-cover.png' WHERE id = 101;

INSERT INTO site_config (id, site_id, config_code, logo_url, hero_video_url, deleted)
VALUES (77, 1, 'media-clear-test', '/uploads/site-logo.png', '/uploads/sites/1/hero-video/hero.mp4', 0);

CREATE TABLE IF NOT EXISTS company_profile (
    id BIGINT PRIMARY KEY,
    profile_code VARCHAR(100),
    company_name VARCHAR(200),
    address VARCHAR(500),
    postal_code VARCHAR(20),
    contact_name VARCHAR(100),
    mobile VARCHAR(30),
    telephone VARCHAR(30),
    fax VARCHAR(30),
    email VARCHAR(150),
    qq VARCHAR(50),
    wechat_image_url VARCHAR(500),
    business_license_no VARCHAR(100),
    other_info TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT DEFAULT 0
);
INSERT INTO company_profile (id, profile_code, company_name, wechat_image_url, deleted)
VALUES (77, 'media-clear-test', 'Test company', '/uploads/company-qr.png', 0);
