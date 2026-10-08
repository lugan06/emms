package com.eems.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eems.entity.CmsContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CmsContentMapper extends BaseMapper<CmsContent> {
    @Select("SELECT COUNT(1) FROM cms_content WHERE category_id = #{categoryId} AND deleted = 0")
    long countActiveByCategoryId(@Param("categoryId") Long categoryId);

    @Select("SELECT id,category_id,exhibition_id,title,slug,cover_url,summary,body,author,source,status," +
            "published_at,sort_order,is_top,is_recommend,view_count,extra_data,created_by,updated_by," +
            "created_at,updated_at,deleted FROM cms_content WHERE slug = #{slug} LIMIT 1")
    CmsContent selectIncludingDeletedBySlug(@Param("slug") String slug);
}
