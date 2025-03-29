package com.lushan.config;

import com.lushan.entity.User;
import com.lushan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("test")
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 清理所有测试数据
        userRepository.deleteAll();

        // 创建管理员用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@example.com");
        admin.setPhone("13800138000");
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin.setPhoneVerified(true);
        admin.addRole("ADMIN");
        admin.addRole("USER");
        userRepository.save(admin);

        // 创建普通测试用户
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("test123"));
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138001");
        testUser.setEnabled(true);
        testUser.setEmailVerified(true);
        testUser.setPhoneVerified(true);
        testUser.addRole("USER");
        userRepository.save(testUser);

        // 创建未验证的测试用户
        User unverifiedUser = new User();
        unverifiedUser.setUsername("unverified");
        unverifiedUser.setPassword(passwordEncoder.encode("test123"));
        unverifiedUser.setEmail("unverified@example.com");
        unverifiedUser.setPhone("13800138002");
        unverifiedUser.setEnabled(true);
        unverifiedUser.setEmailVerified(false);
        unverifiedUser.setPhoneVerified(false);
        unverifiedUser.addRole("USER");
        userRepository.save(unverifiedUser);

        // 创建禁用的测试用户
        User disabledUser = new User();
        disabledUser.setUsername("disabled");
        disabledUser.setPassword(passwordEncoder.encode("test123"));
        disabledUser.setEmail("disabled@example.com");
        disabledUser.setPhone("13800138003");
        disabledUser.setEnabled(false);
        disabledUser.setEmailVerified(true);
        disabledUser.setPhoneVerified(true);
        disabledUser.addRole("USER");
        userRepository.save(disabledUser);

        // 创建锁定的测试用户
        User lockedUser = new User();
        lockedUser.setUsername("locked");
        lockedUser.setPassword(passwordEncoder.encode("test123"));
        lockedUser.setEmail("locked@example.com");
        lockedUser.setPhone("13800138004");
        lockedUser.setEnabled(true);
        lockedUser.setAccountNonLocked(false);
        lockedUser.setEmailVerified(true);
        lockedUser.setPhoneVerified(true);
        lockedUser.addRole("USER");
        userRepository.save(lockedUser);
    }
}