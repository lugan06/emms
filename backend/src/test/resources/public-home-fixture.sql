CREATE TABLE IF NOT EXISTS cms_category (
    id BIGINT PRIMARY KEY, parent_id BIGINT DEFAULT 0, name VARCHAR(100), code VARCHAR(100),
    url_name VARCHAR(100), model_code VARCHAR(20), list_template VARCHAR(100), detail_template VARCHAR(100),
    sort_order INT DEFAULT 0, is_enabled INT DEFAULT 1, show_in_nav INT DEFAULT 1, link_url VARCHAR(500),
    created_at TIMESTAMP, updated_at TIMESTAMP, deleted INT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS cms_content (
    id BIGINT PRIMARY KEY, category_id BIGINT, exhibition_id BIGINT, title VARCHAR(200), slug VARCHAR(200),
    cover_url VARCHAR(500), summary VARCHAR(1000), body TEXT, author VARCHAR(100), source VARCHAR(100),
    status VARCHAR(20), published_at TIMESTAMP, sort_order INT DEFAULT 0, is_top INT DEFAULT 0,
    is_recommend INT DEFAULT 0, view_count INT DEFAULT 0, extra_data TEXT, created_by BIGINT,
    updated_by BIGINT, created_at TIMESTAMP, updated_at TIMESTAMP, deleted INT DEFAULT 0
);
DELETE FROM cms_content;
DELETE FROM cms_category;
INSERT INTO cms_category (id, parent_id, name, code, model_code) VALUES
    (1, 0, '展会概览', 'exhibition-overview', 'DIRECTORY'),
    (2, 1, '展会简介', 'exhibition-intro', 'PAGE'),
    (4, 1, '参展范围', 'exhibition-scope', 'ARTICLE'),
    (9, 0, '同期活动', 'events', 'ARTICLE'),
    (6, 0, '展会动态', 'exhibition-news', 'ARTICLE'),
    (7, 0, '行业资讯', 'industry-news', 'ARTICLE'),
    (8, 0, '展商新闻', 'exhibitor-news', 'ARTICLE');
-- Content IDs deliberately differ from exhibition IDs. ID 1 belongs to another exhibition.
INSERT INTO cms_content (id, category_id, exhibition_id, title, body, status, published_at, is_top) VALUES
    (1, 2, 2, '其他展会简介', '<p>其他展会</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (102, 2, 1, '当前展会简介', '<p>当前展会正文</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (104, 4, 1, '当前参展范围', '<p>参展范围</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (109, 9, 1, '当前同期活动', '<p>同期活动</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (106, 6, 1, '当前展会动态', '<p>展会动态</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (107, 7, NULL, '行业资讯', '<p>行业资讯</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (207, 7, 2, '跨展会行业资讯', '<p>行业资讯</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (108, 8, NULL, '展商新闻', '<p>展商新闻</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (204, 4, 2, '其他参展范围', '<p>其他范围</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (209, 9, 2, '其他同期活动', '<p>其他活动</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (206, 6, 2, '其他展会动态', '<p>其他动态</p>', 'PUBLISHED', '2020-01-01 00:00:00', 0),
    (302, 2, 1, '未发布简介', '<p>草稿</p>', 'DRAFT', NULL, 1),
    (402, 2, 1, '未来发布简介', '<p>未来内容</p>', 'PUBLISHED', '2099-01-01 00:00:00', 1),
    (502, 2, 1, '已删除简介', '<p>已删除</p>', 'PUBLISHED', '2020-01-01 00:00:00', 1),
    (602, 2, 1, '置顶简介', '<p>置顶正文</p>', 'PUBLISHED', '2020-01-01 00:00:00', 1);
UPDATE cms_content SET deleted = 1 WHERE id = 502;
