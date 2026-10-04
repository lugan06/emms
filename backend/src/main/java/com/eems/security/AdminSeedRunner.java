package com.eems.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeedRunner.class);

    private final AdminSeedProperties properties;
    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminSeedRunner(AdminSeedProperties properties,
                           AdminUserMapper adminUserMapper,
                           PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!properties.isEnabled()) {
            return;
        }
        if (isBlank(properties.getUsername()) || isBlank(properties.getPassword())) {
            throw new IllegalStateException("EEMS_SEED_ADMIN_USERNAME and EEMS_SEED_ADMIN_PASSWORD are required when EEMS_SEED_ADMIN_ENABLED=true");
        }

        AdminUser existing = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, properties.getUsername()));
        if (existing != null) {
            log.info("Admin seed skipped because username already exists: {}", properties.getUsername());
            return;
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(properties.getUsername());
        adminUser.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        adminUser.setNickname(properties.getNickname());
        adminUser.setRoleCode(properties.getRoleCode());
        adminUser.setStatus("ENABLED");
        adminUser.setDeleted(0);
        adminUserMapper.insert(adminUser);
        log.info("Initial admin account created: {}", properties.getUsername());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
