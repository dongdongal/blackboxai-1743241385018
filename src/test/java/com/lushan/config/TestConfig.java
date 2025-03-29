package com.lushan.config;

import com.lushan.entity.User;
import com.lushan.service.EmailService;
import com.lushan.service.SmsService;
import com.lushan.service.UserService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.mail.internet.MimeMessage;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public EmailService mockEmailService() {
        return new EmailService(mockJavaMailSender(), null) {
            @Override
            public void sendVerificationEmail(String to, String token) {
                // Do nothing in test environment
            }

            @Override
            public void sendPasswordResetEmail(String to, String token) {
                // Do nothing in test environment
            }

            @Override
            public void sendWelcomeEmail(String to, String username) {
                // Do nothing in test environment
            }
        };
    }

    @Bean
    @Primary
    public SmsService mockSmsService() {
        return new SmsService(null) {
            @Override
            public void sendVerificationCode(String phone) {
                // Do nothing in test environment
            }

            @Override
            public boolean verifyCode(String phone, String code) {
                // Always return true in test environment
                return true;
            }

            @Override
            public String getTestVerificationCode(String phone) {
                return "123456";
            }
        };
    }

    @Bean
    @Primary
    public JavaMailSender mockJavaMailSender() {
        JavaMailSender mockMailSender = Mockito.mock(JavaMailSender.class);
        MimeMessage mockMimeMessage = Mockito.mock(MimeMessage.class);
        Mockito.when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        return mockMailSender;
    }

    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public User testAdminUser(PasswordEncoder passwordEncoder) {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@example.com");
        admin.setEnabled(true);
        admin.addRole("ADMIN");
        return admin;
    }

    @Bean
    public User testRegularUser(PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("test123"));
        user.setEmail("test@example.com");
        user.setEnabled(true);
        user.addRole("USER");
        return user;
    }
}