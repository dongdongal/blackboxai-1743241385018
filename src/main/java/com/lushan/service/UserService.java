package com.lushan.service;

import com.lushan.entity.User;
import com.lushan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SmsService smsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isAccountNonLocked(),
                authorities
        );
    }

    @Transactional
    public User register(User user, String rawPassword) {
        // 验证用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 验证邮箱是否已存在
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 验证手机号是否已存在
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("手机号已被注册");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(rawPassword));

        // 设置默认角色
        user.addRole("USER");

        // 生成邮箱验证token
        if (user.getEmail() != null) {
            user.setEmailVerificationToken(UUID.randomUUID().toString());
            // 发送验证邮件
            emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(User user, HttpServletRequest request) {
        String ip = getClientIp(request);
        user.updateLastLogin(ip);
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now(), ip);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("无效的验证token"));
        
        userRepository.verifyEmail(user.getId());
    }

    @Transactional
    public void verifyPhone(String token) {
        User user = userRepository.findByPhoneVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("无效的验证token"));
        
        userRepository.verifyPhone(user.getId());
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("邮箱未注册"));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpireTime(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // 发送密码重置邮件
        emailService.sendPasswordResetEmail(email, token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new IllegalArgumentException("无效的重置token"));

        if (user.getResetPasswordTokenExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("重置token已过期");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpireTime(null);
        userRepository.save(user);
    }

    @Transactional
    public void updateMembership(Long userId, String membershipType, int months) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        LocalDateTime expireDate = user.getMembershipExpireDate();
        if (expireDate == null || expireDate.isBefore(LocalDateTime.now())) {
            expireDate = LocalDateTime.now();
        }
        
        user.setMembershipType(membershipType);
        user.setMembershipExpireDate(expireDate.plusMonths(months));
        userRepository.save(user);
    }

    // 获取客户端IP地址
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}