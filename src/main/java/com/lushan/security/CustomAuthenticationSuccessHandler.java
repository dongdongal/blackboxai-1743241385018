package com.lushan.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lushan.entity.User;
import com.lushan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        if (isRestRequest(request)) {
            // REST API请求返回JSON响应
            handleRestAuthentication(request, response, authentication);
        } else {
            // 表单登录请求进行重定向
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    private void handleRestAuthentication(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        String token = jwtUtil.generateToken(authentication);

        // 更新最后登录时间和IP
        userService.updateLastLogin(user, request);

        AuthResponse authResponse = AuthResponse.builder()
                .success(true)
                .token(token)
                .username(username)
                .email(user.getEmail())
                .roles(new ArrayList<>(user.getRoles()))
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .message("登录成功")
                .build();

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(authResponse));
    }

    private boolean isRestRequest(HttpServletRequest request) {
        String contentType = request.getHeader("Content-Type");
        String acceptHeader = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");

        return (contentType != null && contentType.contains("application/json")) ||
               (acceptHeader != null && acceptHeader.contains("application/json")) ||
               "XMLHttpRequest".equals(xRequestedWith);
    }
}