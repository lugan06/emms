CREATE TABLE IF NOT EXISTS exhibition (
    id BIGINT PRIMARY KEY,
    site_id BIGINT NOT NULL,
    exhibition_code VARCHAR(100),
    title VARCHAR(200),
    subtitle VARCHAR(200),
    year INT,
    edition VARCHAR(50),
    cover_url VARCHAR(500),
    summary TEXT,
    description TEXT,
    venue VARCHAR(200),
    address VARCHAR(500),
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    status VARCHAR(20),
    show_on_home INT,
    home_sort INT,
    published_at TIMESTAMP,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(30),
    registration_url VARCHAR(500),
    extra_data TEXT,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT DEFAULT 0
);
DELETE FROM exhibition;
INSERT INTO exhibition (id, site_id, exhibition_code, title, year, start_at, end_at, status, deleted)
VALUES (101, 1, 'EXPO-A', 'Site A exhibition', 2027, '2027-01-01 09:00:00', '2027-01-02 17:00:00', 'DRAFT', 0),
       (202, 2, 'EXPO-B', 'Site B exhibition', 2028, '2028-01-01 09:00:00', '2028-01-02 17:00:00', 'DRAFT', 0),
       (203, 2, 'EXPO-B-DELETED', 'Deleted B exhibition', 2028, '2028-02-01 09:00:00', '2028-02-02 17:00:00', 'DRAFT', 1);
INSERT INTO cms_category (id, site_id, parent_id, name, code, model_code, deleted)
VALUES (1002, 2, 0, 'Site B category', 'exhibition-intro', 'ARTICLE', 1),
       (1003, 2, 0, 'Active B category', 'only-b', 'ARTICLE', 0);
INSERT INTO cms_content (id, site_id, category_id, title, slug, status, deleted)
VALUES (1001, 1, 2, 'Site A article', 'shared-slug', 'PUBLISHED', 0),
       (1002, 2, 1002, 'Site B article', 'shared-slug', 'PUBLISHED', 0),
       (1003, 2, 1003, 'Active B article', 'only-b', 'PUBLISHED', 0);
