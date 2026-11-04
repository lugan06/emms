package com.eems.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.eems.entity.Site;
import com.eems.entity.SiteDomain;
import org.apache.ibatis.annotations.*;
import java.util.List;

/** Global management SQL; all values are bound parameters. */
@Mapper
public interface SiteManagementMapper extends BaseMapper<Site> {
    @Select("SELECT * FROM site WHERE id = #{id} FOR UPDATE")
    Site lockSite(@Param("id") Long id);

    @Select("""
            <script>
            SELECT site_id, domain, is_primary FROM site_domain WHERE site_id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
            ORDER BY is_primary DESC, domain
            </script>
            """)
    @Results({
            @Result(column = "site_id", property = "siteId"),
            @Result(column = "is_primary", property = "primary")
    })
    List<SiteDomain> selectDomains(@Param("ids") List<Long> ids);

    @Select("SELECT COUNT(*) FROM site_domain WHERE domain = #{domain} AND site_id != #{siteId}")
    int countOtherDomain(@Param("domain") String domain, @Param("siteId") Long siteId);

    @Insert("INSERT INTO site_domain (site_id, domain, is_primary) VALUES (#{siteId}, #{domain}, #{primary})")
    int insertDomain(@Param("siteId") Long siteId, @Param("domain") String domain, @Param("primary") boolean primary);

    @Delete("DELETE FROM site_domain WHERE site_id = #{siteId}")
    int deleteDomains(@Param("siteId") Long siteId);

    // Narrow exception: only initialize the explicitly supplied new site; never mutate SiteContext.
    @InterceptorIgnore(tenantLine = "true")
    @Insert("""
            INSERT INTO site_config (site_id, config_code, site_title, domain, deleted, created_at, updated_at)
            VALUES (#{siteId}, 'default', #{name}, #{domain}, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """)
    int initializeConfig(@Param("siteId") Long siteId, @Param("name") String name, @Param("domain") String domain);
}

