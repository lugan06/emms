CREATE TABLE IF NOT EXISTS site (
    id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL
);
CREATE TABLE IF NOT EXISTS site_domain (
    id BIGINT PRIMARY KEY,
    site_id BIGINT NOT NULL,
    domain VARCHAR(253) NOT NULL UNIQUE,
    is_primary TINYINT NOT NULL,
    FOREIGN KEY (site_id) REFERENCES site(id)
);
MERGE INTO site (id, name, code, status) KEY (id) VALUES (1, 'Default site', 'default', 'ENABLED');
MERGE INTO site_domain (id, site_id, domain, is_primary) KEY (id) VALUES (1, 1, 'localhost', 0);
MERGE INTO site_domain (id, site_id, domain, is_primary) KEY (id) VALUES (2, 1, '127.0.0.1', 0);

CREATE TABLE IF NOT EXISTS site_config (
    id BIGINT PRIMARY KEY,
    site_id BIGINT NOT NULL DEFAULT 1,
    config_code VARCHAR(100),
    current_exhibition_id BIGINT,
    hero_video_url VARCHAR(500),
    site_title VARCHAR(200),
    site_subtitle VARCHAR(200),
    domain VARCHAR(255),
    logo_url VARCHAR(500),
    keywords VARCHAR(500),
    description TEXT,
    icp_number VARCHAR(100),
    template_code VARCHAR(100),
    statistics_code TEXT,
    footer_info TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT DEFAULT 0
);
