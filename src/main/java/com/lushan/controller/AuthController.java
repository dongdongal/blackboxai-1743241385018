package com.lushan.controller;

import com.lushan.entity.User;
import com.lushan.security.AuthResponse;
import com.lushan.security.JwtUtil;
import com.lushan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                 @RequestParam String password,
                                 HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            User user = userService.findByUsername(username);
            userService.updateLastLogin(user, request);

            return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(new ArrayList<>(user.getRoles()))
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .build());

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                .success(false)
                .message("用户名或密码错误")
                .build());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user,
                                    @RequestParam String password,
                                    @RequestParam(required = false) String verificationCode) {
        try {
            // 如果提供了手机号，验证短信验证码
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                if (verificationCode == null || verificationCode.isEmpty()) {
                    return ResponseEntity.badRequest().body(AuthResponse.builder()
                        .success(false)
                        .message("请提供验证码")
                        .build());
                }
                
                if (!userService.verifyPhoneCode(user.getPhone(), verificationCode)) {
                    return ResponseEntity.badRequest().body(AuthResponse.builder()
                        .success(false)
                        .message("验证码错误或已过期")
                        .build());
                }
            }

            User registeredUser = userService.register(user, password);
            UserDetails userDetails = userService.loadUserByUsername(registeredUser.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .token(token)
                .username(registeredUser.getUsername())
                .email(registeredUser.getEmail())
                .roles(new ArrayList<>(registeredUser.getRoles()))
                .emailVerified(registeredUser.isEmailVerified())
                .phoneVerified(registeredUser.isPhoneVerified())
                .message("注册成功")
                .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build());
        }
    }

    @PostMapping("/verify-email/{token}")
    public ResponseEntity<?> verifyEmail(@PathVariable String token) {
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .message("邮箱验证成功")
                .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build());
        }
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String phone) {
        try {
            userService.sendPhoneVerificationCode(phone);
            return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .message("验证码已发送")
                .build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build());
        }
    }

    @PostMapping("/reset-password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        try {
            userService.initiatePasswordReset(email);
            return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .message("密码重置链接已发送到您的邮箱")
                .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build());
        }
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestParam String token,
                                                @RequestParam String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .message("密码重置成功")
                .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        
        if (token != null && jwtUtil.needsRefresh(token)) {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userService.loadUserByUsername(username);
            
            if (jwtUtil.validateToken(token, userDetails)) {
                String newToken = jwtUtil.refreshToken(token);
                return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .token(newToken)
                    .build());
            }
        }
        
        return ResponseEntity.badRequest().body(AuthResponse.builder()
            .success(false)
            .message("无效的token或token不需要刷新")
            .build());
    }
}