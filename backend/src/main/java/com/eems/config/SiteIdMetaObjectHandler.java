package com.eems.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.eems.entity.OperationLog;
import com.eems.site.SiteContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class SiteIdMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasSetter("siteId")) {
            Long siteId = metaObject.getOriginalObject() instanceof OperationLog
                    ? SiteContext.currentSiteIdOrNull() : SiteContext.requireSiteId();
            metaObject.setValue("siteId", siteId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
    }
}
