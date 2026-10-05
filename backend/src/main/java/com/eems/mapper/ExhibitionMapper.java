package com.eems.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eems.entity.Exhibition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ExhibitionMapper extends BaseMapper<Exhibition> {
    @Update("UPDATE exhibition SET deleted = 0, updated_by = #{updatedBy}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id, @Param("updatedBy") Long updatedBy);
}
