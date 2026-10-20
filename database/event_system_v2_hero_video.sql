USE event_system;

ALTER TABLE site_config
    ADD COLUMN hero_video_url VARCHAR(500) NULL;
