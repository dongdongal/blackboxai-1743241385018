package com.lushan.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final StringRedisTemplate redisTemplate;
    private static final String VERIFICATION_CODE_PREFIX = "sms:verify:";
    private static final Duration CODE_EXPIRE_TIME = Duration.ofMinutes(5);
    private static final Duration RESEND_LIMIT_TIME = Duration.ofMinutes(1);

    @Async
    public void sendVerificationCode(String phone) {
        // 检查是否可以发送验证码（防止频繁发送）
        String limitKey = VERIFICATION_CODE_PREFIX + "limit:" + phone;
        Boolean canSend = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", RESEND_LIMIT_TIME);
        
        if (Boolean.FALSE.equals(canSend)) {
            throw new IllegalStateException("请稍后再试");
        }

        // 生成6位验证码
        String code = generateVerificationCode();
        
        try {
            // TODO: 调用实际的短信发送服务
            // 这里模拟发送短信
            simulateSendSms(phone, code);
            
            // 将验证码保存到Redis，设置5分钟过期
            String codeKey = VERIFICATION_CODE_PREFIX + phone;
            redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_TIME);
            
        } catch (Exception e) {
            // 发送失败时删除限制标记
            redisTemplate.delete(limitKey);
            throw new RuntimeException("发送验证码失败: " + e.getMessage(), e);
        }
    }

    public boolean verifyCode(String phone, String code) {
        String codeKey = VERIFICATION_CODE_PREFIX + phone;
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (savedCode != null && savedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(codeKey);
            return true;
        }
        
        return false;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void simulateSendSms(String phone, String code) {
        // 模拟发送短信的延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 实际项目中，这里需要调用短信服务提供商的API
        System.out.println("Sending SMS to " + phone + ": Your verification code is " + code);
    }

    // 用于测试环境获取验证码
    public String getTestVerificationCode(String phone) {
        if (!isTestEnvironment()) {
            throw new IllegalStateException("此方法仅用于测试环境");
        }
        String codeKey = VERIFICATION_CODE_PREFIX + phone;
        return redisTemplate.opsForValue().get(codeKey);
    }

    private boolean isTestEnvironment() {
        String activeProfile = System.getProperty("spring.profiles.active");
        return activeProfile != null && activeProfile.contains("test");
    }
}