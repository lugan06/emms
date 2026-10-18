package com.eems.site;

import com.eems.config.SiteIdMetaObjectHandler;
import com.eems.entity.CmsCategory;
import com.eems.entity.CmsContent;
import com.eems.entity.Exhibition;
import com.eems.entity.FileAsset;
import com.eems.entity.Guestbook;
import com.eems.entity.OperationLog;
import com.eems.entity.SiteConfig;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SiteIdMetaObjectHandlerTest {
    private final SiteIdMetaObjectHandler handler = new SiteIdMetaObjectHandler();

    @AfterEach
    void clearSite() {
        SiteContext.clear();
    }

    @Test
    void overwritesProvidedSiteIdForEveryScopedEntity() {
        SiteContext.setSiteId(1L);
        for (Object entity : List.of(new SiteConfig(), new Exhibition(), new CmsCategory(),
                new CmsContent(), new Guestbook(), new FileAsset())) {
            MetaObject metaObject = SystemMetaObject.forObject(entity);
            metaObject.setValue("siteId", 2L);
            handler.insertFill(metaObject);
            assertEquals(1L, metaObject.getValue("siteId"));
        }
    }

    @Test
    void businessInsertRequiresContextButGlobalLogMayHaveNone() {
        assertThrows(IllegalStateException.class,
                () -> handler.insertFill(SystemMetaObject.forObject(new Exhibition())));
        OperationLog log = new OperationLog();
        handler.insertFill(SystemMetaObject.forObject(log));
        assertNull(log.getSiteId());

        SiteContext.setSiteId(2L);
        handler.insertFill(SystemMetaObject.forObject(log));
        assertEquals(2L, log.getSiteId());
    }
}
