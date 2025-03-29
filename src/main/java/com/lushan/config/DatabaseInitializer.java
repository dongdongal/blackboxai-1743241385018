package com.lushan.config;

import com.lushan.entity.User;
import com.lushan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class DatabaseInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("Initializing database...");
            
            // 执行schema.sql
            executeScript("schema.sql");
            
            // 检查是否需要初始化数据
            if (userRepository.count() == 0) {
                log.info("Database is empty, initializing with default data...");
                
                // 创建默认用户
                createDefaultUsers();
                
                // 执行data.sql
                executeScript("data.sql");
                
                log.info("Database initialization completed successfully.");
            } else {
                log.info("Database already contains data, skipping initialization.");
            }
        } catch (Exception e) {
            log.error("Error initializing database", e);
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    private void executeScript(String scriptName) {
        try {
            log.info("Executing SQL script: {}", scriptName);
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource(scriptName));
            populator.execute(dataSource);
        } catch (Exception e) {
            log.error("Error executing SQL script: {}", scriptName, e);
            throw new RuntimeException("Could not execute SQL script: " + scriptName, e);
        }
    }

    private void createDefaultUsers() {
        log.info("Creating default users...");

        List<User> defaultUsers = Arrays.asList(
            createUser("admin", "admin123", "admin@lushan-garden.com", "13800138000", true, "ADMIN", "USER"),
            createUser("testuser", "test123", "test@lushan-garden.com", "13800138001", true, "USER"),
            createUser("unverified", "test123", "unverified@lushan-garden.com", "13800138002", false, "USER"),
            createUser("disabled", "test123", "disabled@lushan-garden.com", "13800138003", true, "USER")
        );

        for (User user : defaultUsers) {
            try {
                userRepository.save(user);
                log.info("Created user: {}", user.getUsername());
            } catch (Exception e) {
                log.error("Error creating user: {}", user.getUsername(), e);
            }
        }
    }

    private User createUser(String username, String password, String email, String phone, boolean verified, String... roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setEnabled(true);
        user.setEmailVerified(verified);
        user.setPhoneVerified(verified);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 特殊处理
        if ("disabled".equals(username)) {
            user.setEnabled(false);
        }
        
        // 添加角色
        Arrays.stream(roles).forEach(user::addRole);
        
        return user;
    }
}