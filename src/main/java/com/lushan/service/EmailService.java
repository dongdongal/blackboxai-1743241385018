package com.lushan.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendVerificationEmail(String to, String token) {
        Context context = new Context();
        context.setVariable("token", token);
        context.setVariable("verifyUrl", "http://localhost:8080/verify-email?token=" + token);

        String content = templateEngine.process("mail/verify-email", context);
        sendHtmlEmail(to, "请验证您的邮箱", content);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        Context context = new Context();
        context.setVariable("token", token);
        context.setVariable("resetUrl", "http://localhost:8080/reset-password?token=" + token);

        String content = templateEngine.process("mail/reset-password", context);
        sendHtmlEmail(to, "密码重置请求", content);
    }

    @Async
    public void sendWelcomeEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);

        String content = templateEngine.process("mail/welcome", context);
        sendHtmlEmail(to, "欢迎加入庐山杏林中药植物研究所", content);
    }

    private void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("noreply@lushan-garden.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("发送邮件失败: " + e.getMessage(), e);
        }
    }
}