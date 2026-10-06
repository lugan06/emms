package com.eems.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CmsContentMapper {
    @Select("SELECT COUNT(1) FROM cms_content WHERE category_id = #{categoryId} AND deleted = 0")
    long countActiveByCategoryId(@Param("categoryId") Long categoryId);
}
