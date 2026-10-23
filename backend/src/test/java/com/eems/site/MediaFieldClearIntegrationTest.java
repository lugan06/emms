package com.eems.site;

import com.eems.entity.CmsContent;
import com.eems.entity.CompanyProfile;
import com.eems.entity.Exhibition;
import com.eems.entity.SiteConfig;
import com.eems.mapper.CmsContentMapper;
import com.eems.mapper.CompanyProfileMapper;
import com.eems.mapper.ExhibitionMapper;
import com.eems.mapper.SiteConfigMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
@Sql({"/public-home-fixture.sql", "/site-data-isolation-fixture.sql", "/media-clear-fixture.sql"})
class MediaFieldClearIntegrationTest {
    @Autowired private CmsContentMapper contentMapper;
    @Autowired private ExhibitionMapper exhibitionMapper;
    @Autowired private SiteConfigMapper siteConfigMapper;
    @Autowired private CompanyProfileMapper companyProfileMapper;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void selectDefaultSite() {
        SiteContext.setSiteId(1L);
    }

    @AfterEach
    void clearSite() {
        SiteContext.clear();
    }

    @Test
    void mapperUpdatesPersistNullForAllClearableMediaUrls() {
        CmsContent content = contentMapper.selectById(1001L);
        content.setCoverUrl(null);
        assertEquals(1, contentMapper.updateById(content));
        assertNull(jdbc.queryForObject("SELECT cover_url FROM cms_content WHERE id = 1001", String.class));

        Exhibition exhibition = exhibitionMapper.selectById(101L);
        exhibition.setCoverUrl(null);
        assertEquals(1, exhibitionMapper.updateById(exhibition));
        assertNull(jdbc.queryForObject("SELECT cover_url FROM exhibition WHERE id = 101", String.class));

        SiteConfig siteConfig = siteConfigMapper.selectById(77L);
        siteConfig.setLogoUrl(null);
        siteConfig.setHeroVideoUrl(null);
        assertEquals(1, siteConfigMapper.updateById(siteConfig));
        assertNull(jdbc.queryForObject("SELECT logo_url FROM site_config WHERE id = 77", String.class));
        assertNull(jdbc.queryForObject("SELECT hero_video_url FROM site_config WHERE id = 77", String.class));

        CompanyProfile company = companyProfileMapper.selectById(77L);
        company.setWechatImageUrl(null);
        assertEquals(1, companyProfileMapper.updateById(company));
        assertNull(jdbc.queryForObject("SELECT wechat_image_url FROM company_profile WHERE id = 77", String.class));
    }
}
