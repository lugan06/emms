package com.eems.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eems.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
    @Select("SELECT * FROM admin_user WHERE id = #{id} AND role_code = 'EDITOR' AND deleted = 0 FOR UPDATE")
    AdminUser lockEditor(@Param("id") Long id);
}
