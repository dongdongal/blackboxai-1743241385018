package com.lushan.util;

import com.lushan.entity.User;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestUtils {

    private static final Random RANDOM = new Random();

    /**
     * 创建测试用户
     */
    public static User createTestUser() {
        User user = new User();
        user.setUsername(generateRandomUsername());
        user.setEmail(generateRandomEmail());
        user.setPhone(generateRandomPhone());
        user.setPassword("Test@123");
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setPhoneVerified(true);
        user.addRole("USER");
        return user;
    }

    /**
     * 创建测试管理员
     */
    public static User createTestAdmin() {
        User admin = createTestUser();
        admin.addRole("ADMIN");
        return admin;
    }

    /**
     * 生成随机用户名
     */
    public static String generateRandomUsername() {
        return "test_user_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(1000);
    }

    /**
     * 生成随机邮箱
     */
    public static String generateRandomEmail() {
        return "test_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(1000) + "@example.com";
    }

    /**
     * 生成随机手机号
     */
    public static String generateRandomPhone() {
        return "138" + String.format("%08d", RANDOM.nextInt(100000000));
    }

    /**
     * 生成随机密码
     */
    public static String generateRandomPassword() {
        return "Test@" + RANDOM.nextInt(1000000);
    }

    /**
     * 创建模拟认证
     */
    public static void setMockAuthentication(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user.getUsername(),
            user.getPassword(),
            user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * 清除认证
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 创建模拟文件
     */
    public static MockMultipartFile createMockFile(String name, String contentType, byte[] content) {
        return new MockMultipartFile(
            name,
            name + "." + contentType.split("/")[1],
            contentType,
            content
        );
    }

    /**
     * 创建模拟图片文件
     */
    public static MockMultipartFile createMockImageFile() {
        return createMockFile(
            "test-image",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 添加认证头到请求
     */
    public static MockHttpServletRequestBuilder addAuthHeader(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    /**
     * 生成随机UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成过去的时间
     */
    public static LocalDateTime getPastDateTime(long minutesAgo) {
        return LocalDateTime.now().minusMinutes(minutesAgo);
    }

    /**
     * 生成未来的时间
     */
    public static LocalDateTime getFutureDateTime(long minutesLater) {
        return LocalDateTime.now().plusMinutes(minutesLater);
    }

    /**
     * 生成随机字符串
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成验证码
     */
    public static String generateVerificationCode() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    /**
     * 生成Bearer token
     */
    public static String getBearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * 设置模拟用户角色
     */
    public static void setUserRole(String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "testuser",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}