package com.eems.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserMapper adminUserMapper;

    public AdminUserDetailsService(AdminUserMapper adminUserMapper) {
        this.adminUserMapper = adminUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)
                .eq(AdminUser::getStatus, "ENABLED"));
        if (adminUser == null) {
            throw new UsernameNotFoundException("管理员不存在或已禁用");
        }
        return User.withUsername(adminUser.getUsername())
                .password(adminUser.getPasswordHash())
                .roles(adminUser.getRoleCode())
                .disabled(false)
                .build();
    }
}
