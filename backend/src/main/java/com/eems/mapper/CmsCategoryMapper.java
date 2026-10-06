package com.eems.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eems.entity.CmsCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CmsCategoryMapper extends BaseMapper<CmsCategory> {
    @Select("SELECT id,parent_id,name,code,url_name,model_code,list_template,detail_template,sort_order," +
            "is_enabled,show_in_nav,link_url,created_at,updated_at,deleted " +
            "FROM cms_category WHERE code = #{code} LIMIT 1")
    CmsCategory selectIncludingDeletedByCode(@Param("code") String code);

    @Select("SELECT id,parent_id,name,code,url_name,model_code,list_template,detail_template,sort_order," +
            "is_enabled,show_in_nav,link_url,created_at,updated_at,deleted " +
            "FROM cms_category WHERE id = #{id} AND deleted = 1 LIMIT 1")
    CmsCategory selectDeletedById(@Param("id") Long id);

    @Update("UPDATE cms_category SET deleted = 0, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);
}
