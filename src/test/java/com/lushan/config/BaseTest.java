package com.lushan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lushan.security.JwtUtil;
import com.lushan.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserService userService;

    @Autowired
    protected JwtUtil jwtUtil;

    protected String adminToken;
    protected String userToken;

    @BeforeEach
    void setUp() throws Exception {
        // 为管理员用户生成token
        adminToken = jwtUtil.generateToken(userService.loadUserByUsername("admin"));
        
        // 为普通用户生成token
        userToken = jwtUtil.generateToken(userService.loadUserByUsername("testuser"));
    }

    /**
     * 获取带有Bearer前缀的认证头
     */
    protected String getBearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * 获取管理员认证头
     */
    protected String getAdminAuthHeader() {
        return getBearerToken(adminToken);
    }

    /**
     * 获取用户认证头
     */
    protected String getUserAuthHeader() {
        return getBearerToken(userToken);
    }

    /**
     * 生成随机用户名
     */
    protected String generateRandomUsername() {
        return "test_user_" + System.currentTimeMillis();
    }

    /**
     * 生成随机邮箱
     */
    protected String generateRandomEmail() {
        return "test_" + System.currentTimeMillis() + "@example.com";
    }

    /**
     * 生成随机手机号
     */
    protected String generateRandomPhone() {
        return "138" + String.format("%08d", System.currentTimeMillis() % 100000000);
    }
}