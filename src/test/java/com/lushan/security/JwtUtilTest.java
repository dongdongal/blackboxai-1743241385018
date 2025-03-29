package com.lushan.security;

import com.lushan.config.BaseTest;
import com.lushan.entity.User;
import com.lushan.util.TestUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest extends BaseTest {

    @Test
    void testGenerateAndValidateToken() {
        // 创建测试用户
        User testUser = TestUtils.createTestUser();
        UserDetails userDetails = userService.loadUserByUsername(testUser.getUsername());

        // 生成token
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token, "Token should not be null");

        // 验证token
        assertTrue(jwtUtil.validateToken(token, userDetails), "Token should be valid");
    }

    @Test
    void testExtractUsername() {
        // 创建测试用户
        User testUser = TestUtils.createTestUser();
        UserDetails userDetails = userService.loadUserByUsername(testUser.getUsername());

        // 生成token并提取用户名
        String token = jwtUtil.generateToken(userDetails);
        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(testUser.getUsername(), extractedUsername, "Extracted username should match");
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // 创建一个短期token
        User testUser = TestUtils.createTestUser();
        UserDetails userDetails = userService.loadUserByUsername(testUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        // 验证token未过期
        assertFalse(jwtUtil.isTokenExpired(token), "Token should not be expired initially");

        // 等待token过期
        Thread.sleep(1000); // 假设token有效期很短

        // 验证token是否需要刷新
        assertTrue(jwtUtil.needsRefresh(token), "Token should need refresh");
    }

    @Test
    void testRefreshToken() {
        // 创建原始token
        User testUser = TestUtils.createTestUser();
        UserDetails userDetails = userService.loadUserByUsername(testUser.getUsername());
        String originalToken = jwtUtil.generateToken(userDetails);

        // 刷新token
        String refreshedToken = jwtUtil.refreshToken(originalToken);

        // 验证新旧token不同
        assertNotEquals(originalToken, refreshedToken, "Refreshed token should be different");

        // 验证新token有效
        assertTrue(jwtUtil.validateToken(refreshedToken, userDetails), "Refreshed token should be valid");
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(JwtException.class, () -> {
            jwtUtil.validateToken(invalidToken, null);
        }, "Should throw exception for invalid token");
    }

    @Test
    void testExtractExpiration() {
        // 创建token
        User testUser = TestUtils.createTestUser();
        UserDetails userDetails = userService.loadUserByUsername(testUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        // 提取过期时间
        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration, "Expiration date should not be null");
        assertTrue(expiration.after(new Date()), "Token should not be expired");
    }

    @Test
    void testExtractAllClaims() {
        // 创建token
        User testUser = TestUtils.createTestUser();
        UserDetails userDetails = userService.loadUserByUsername(testUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        // 提取所有claims
        Claims claims = jwtUtil.extractAllClaims(token);
        assertNotNull(claims, "Claims should not be null");
        assertEquals(testUser.getUsername(), claims.getSubject(), "Subject should match username");
    }

    @Test
    void testExtractTokenFromHeader() {
        String token = "test.token.here";
        String authHeader = "Bearer " + token;

        String extractedToken = jwtUtil.extractTokenFromHeader(authHeader);
        assertEquals(token, extractedToken, "Extracted token should match");

        // 测试无Bearer前缀
        assertNull(jwtUtil.extractTokenFromHeader(token), "Should return null for header without Bearer prefix");

        // 测试null header
        assertNull(jwtUtil.extractTokenFromHeader(null), "Should return null for null header");
    }
}