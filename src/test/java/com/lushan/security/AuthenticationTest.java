package com.lushan.security;

import com.lushan.config.BaseTest;
import com.lushan.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationTest extends BaseTest {

    @Test
    void testSuccessfulRegistration() throws Exception {
        String username = generateRandomUsername();
        String email = generateRandomEmail();
        String phone = generateRandomPhone();

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .param("password", "Test@123")
                .param("verificationCode", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testRegistrationWithExistingUsername() throws Exception {
        User user = new User();
        user.setUsername("admin"); // 使用已存在的用户名
        user.setEmail(generateRandomEmail());
        user.setPhone(generateRandomPhone());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .param("password", "Test@123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .param("username", "testuser")
                .param("password", "test123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .param("username", "testuser")
                .param("password", "wrongpassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void testLoginWithDisabledAccount() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .param("username", "disabled")
                .param("password", "test123"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("账号已被禁用"));
    }

    @Test
    void testLoginWithLockedAccount() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .param("username", "locked")
                .param("password", "test123"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("账号已被锁定"));
    }

    @Test
    void testSuccessfulLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", getUserAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("退出登录成功"));
    }

    @Test
    void testPasswordResetFlow() throws Exception {
        // 1. 请求密码重置
        mockMvc.perform(post("/api/auth/reset-password/request")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密码重置链接已发送到您的邮箱"));

        // 2. 使用重置token更改密码
        String newPassword = "NewTest@123";
        mockMvc.perform(post("/api/auth/reset-password/confirm")
                .param("token", "test-reset-token")
                .param("newPassword", newPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密码重置成功"));

        // 3. 使用新密码登录
        mockMvc.perform(post("/api/auth/login")
                .param("username", "testuser")
                .param("password", newPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testTokenRefresh() throws Exception {
        // 1. 获取初始token
        String initialToken = userToken;

        // 2. 请求刷新token
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", getBearerToken(initialToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // 3. 验证新token不同于旧token
        String response = refreshResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);
        assertNotEquals(initialToken, authResponse.getToken(), "新token应该与旧token不同");

        // 4. 验证新token可用
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", getBearerToken(authResponse.getToken())))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidTokenAccess() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        // 普通用户尝试访问管理员资源
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", getUserAuthHeader()))
                .andExpect(status().isForbidden());
    }
}