package com.lushan.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                              HttpServletResponse response,
                              Authentication authentication) throws IOException, ServletException {
        
        if (isRestRequest(request)) {
            handleRestLogout(response);
        } else {
            // 重定向到登录页面
            response.sendRedirect("/login?logout=true");
        }
    }

    private void handleRestLogout(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        AuthResponse authResponse = AuthResponse.builder()
                .success(true)
                .message("退出登录成功")
                .build();

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