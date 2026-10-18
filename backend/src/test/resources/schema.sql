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
