package com.lushan.security;

import com.lushan.config.BaseTest;
import com.lushan.entity.User;
import com.lushan.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest extends BaseTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestUtils.createTestUser();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUsername() {
        // 设置认证上下文
        TestUtils.setMockAuthentication(testUser);

        // 测试获取当前用户名
        String currentUsername = SecurityUtils.getCurrentUsername();
        assertEquals(testUser.getUsername(), currentUsername, "Current username should match");

        // 清除认证上下文后测试
        SecurityContextHolder.clearContext();
        assertNull(SecurityUtils.getCurrentUsername(), "Should return null when no authentication present");
    }

    @Test
    void testPasswordValidation() {
        // 测试有效密码
        assertTrue(SecurityUtils.isPasswordStrong("Test@123"), "Valid password should pass validation");

        // 测试无效密码
        assertFalse(SecurityUtils.isPasswordStrong("weak"), "Weak password should fail validation");
        assertFalse(SecurityUtils.isPasswordStrong("NoSpecialChar1"), "Password without special char should fail");
        assertFalse(SecurityUtils.isPasswordStrong("no@upper1"), "Password without uppercase should fail");
        assertFalse(SecurityUtils.isPasswordStrong("NO@LOWER1"), "Password without lowercase should fail");
        assertFalse(SecurityUtils.isPasswordStrong("No@Special"), "Password without number should fail");
    }

    @Test
    void testUsernameValidation() {
        // 测试有效用户名
        assertTrue(SecurityUtils.isValidUsername("validuser"), "Valid username should pass validation");
        assertTrue(SecurityUtils.isValidUsername("valid_user"), "Username with underscore should pass");
        assertTrue(SecurityUtils.isValidUsername("valid-user"), "Username with hyphen should pass");

        // 测试无效用户名
        assertFalse(SecurityUtils.isValidUsername("ab"), "Too short username should fail");
        assertFalse(SecurityUtils.isValidUsername("invalid@user"), "Username with special chars should fail");
        assertFalse(SecurityUtils.isValidUsername(""), "Empty username should fail");
    }

    @Test
    void testEmailValidation() {
        // 测试有效邮箱
        assertTrue(SecurityUtils.isValidEmail("test@example.com"), "Valid email should pass validation");
        assertTrue(SecurityUtils.isValidEmail("test.user@example.com"), "Email with dot should pass");
        assertTrue(SecurityUtils.isValidEmail("test+user@example.com"), "Email with plus should pass");

        // 测试无效邮箱
        assertFalse(SecurityUtils.isValidEmail("invalid.email"), "Invalid email should fail");
        assertFalse(SecurityUtils.isValidEmail("@example.com"), "Email without local part should fail");
        assertFalse(SecurityUtils.isValidEmail("test@"), "Email without domain should fail");
    }

    @Test
    void testPhoneValidation() {
        // 测试有效手机号
        assertTrue(SecurityUtils.isValidPhone("13800138000"), "Valid phone number should pass validation");
        assertTrue(SecurityUtils.isValidPhone("15900159000"), "Valid phone number should pass");

        // 测试无效手机号
        assertFalse(SecurityUtils.isValidPhone("12345678901"), "Invalid phone number should fail");
        assertFalse(SecurityUtils.isValidPhone("1380013800"), "Too short phone number should fail");
        assertFalse(SecurityUtils.isValidPhone("138001380001"), "Too long phone number should fail");
    }

    @Test
    void testRandomPasswordGeneration() {
        String password = SecurityUtils.generateRandomPassword();
        assertNotNull(password, "Generated password should not be null");
        assertTrue(SecurityUtils.isPasswordStrong(password), "Generated password should be strong");
    }

    @Test
    void testVerificationCodeGeneration() {
        String code = SecurityUtils.generateVerificationCode();
        assertNotNull(code, "Generated code should not be null");
        assertEquals(6, code.length(), "Verification code should be 6 digits");
        assertTrue(code.matches("\\d{6}"), "Verification code should only contain digits");
    }

    @Test
    void testRoleChecking() {
        // 设置用户角色
        TestUtils.setUserRole("USER");

        // 测试角色检查
        assertTrue(SecurityUtils.hasRole("USER"), "Should have USER role");
        assertFalse(SecurityUtils.hasRole("ADMIN"), "Should not have ADMIN role");

        // 测试管理员检查
        assertFalse(SecurityUtils.isAdmin(), "Should not be admin");

        // 切换到管理员角色
        TestUtils.setUserRole("ADMIN");
        assertTrue(SecurityUtils.isAdmin(), "Should be admin");
    }

    @Test
    void testAuthenticationStatus() {
        assertFalse(SecurityUtils.isAuthenticated(), "Should not be authenticated initially");

        // 设置认证
        TestUtils.setMockAuthentication(testUser);
        assertTrue(SecurityUtils.isAuthenticated(), "Should be authenticated after setting context");

        // 清除认证
        TestUtils.clearAuthentication();
        assertFalse(SecurityUtils.isAuthenticated(), "Should not be authenticated after clearing context");
    }

    @Test
    void testPasswordEncryption() {
        String rawPassword = "Test@123";
        String encodedPassword = SecurityUtils.encryptPassword(rawPassword);

        assertNotEquals(rawPassword, encodedPassword, "Encoded password should be different from raw password");
        assertTrue(SecurityUtils.matchesPassword(rawPassword, encodedPassword), "Password should match its encoded version");
        assertFalse(SecurityUtils.matchesPassword("WrongPassword", encodedPassword), "Wrong password should not match");
    }
}