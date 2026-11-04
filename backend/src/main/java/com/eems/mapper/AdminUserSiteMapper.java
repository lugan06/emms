package com.eems.mapper;

import com.eems.entity.AdminUserSite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** Global bindings: callers must authorize management operations before using this mapper. */
@Mapper
public interface AdminUserSiteMapper {
    // Scalar parameters bypass entity site auto-fill; the database supplies created_at.
    @Insert("INSERT INTO admin_user_site (admin_user_id, site_id) VALUES (#{adminUserId}, #{siteId})")
    int insertBinding(@Param("adminUserId") Long adminUserId, @Param("siteId") Long siteId);

    @Select("SELECT admin_user_id, site_id, created_at FROM admin_user_site "
            + "WHERE admin_user_id = #{adminUserId} AND site_id = #{siteId}")
    @Results(id = "adminUserSite", value = {
            @Result(column = "admin_user_id", property = "adminUserId", id = true),
            @Result(column = "site_id", property = "siteId", id = true),
            @Result(column = "created_at", property = "createdAt")
    })
    AdminUserSite selectBinding(@Param("adminUserId") Long adminUserId, @Param("siteId") Long siteId);

    @Select("SELECT admin_user_id, site_id, created_at FROM admin_user_site "
            + "WHERE admin_user_id = #{adminUserId} ORDER BY site_id")
    @ResultMap("adminUserSite")
    List<AdminUserSite> selectByAdminUserId(@Param("adminUserId") Long adminUserId);

    @Select("SELECT COUNT(*) FROM admin_user_site "
            + "WHERE admin_user_id = #{adminUserId} AND site_id = #{siteId}")
    boolean existsBinding(@Param("adminUserId") Long adminUserId, @Param("siteId") Long siteId);

    @Delete("DELETE FROM admin_user_site WHERE admin_user_id = #{adminUserId} AND site_id = #{siteId}")
    int deleteBinding(@Param("adminUserId") Long adminUserId, @Param("siteId") Long siteId);

    @Delete("DELETE FROM admin_user_site WHERE admin_user_id = #{adminUserId}")
    int deleteByAdminUserId(@Param("adminUserId") Long adminUserId);
}
