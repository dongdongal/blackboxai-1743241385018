package com.lushan.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                      HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        if (isRestRequest(request)) {
            handleRestAuthenticationFailure(response, exception);
        } else {
            // 为表单登录添加错误消息
            String errorMessage = getErrorMessage(exception);
            String redirectUrl = getFailureUrl() + "?error=true&message=" + errorMessage;
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }

    private void handleRestAuthenticationFailure(HttpServletResponse response,
                                               AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        AuthResponse authResponse = AuthResponse.builder()
                .success(false)
                .message(getErrorMessage(exception))
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(authResponse));
    }

    private String getErrorMessage(AuthenticationException exception) {
        String message;
        String exceptionName = exception.getClass().getSimpleName();
        
        switch (exceptionName) {
            case "BadCredentialsException":
                message = "用户名或密码错误";
                break;
            case "DisabledException":
                message = "账号已被禁用";
                break;
            case "LockedException":
                message = "账号已被锁定";
                break;
            case "AccountExpiredException":
                message = "账号已过期";
                break;
            case "CredentialsExpiredException":
                message = "密码已过期";
                break;
            default:
                message = "登录失败: " + exception.getMessage();
        }
        
        return message;
    }

    private boolean isRestRequest(HttpServletRequest request) {
        String contentType = request.getHeader("Content-Type");
        String acceptHeader = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");

        return (contentType != null && contentType.contains("application/json")) ||
               (acceptHeader != null && acceptHeader.contains("application/json")) ||
               "XMLHttpRequest".equals(xRequestedWith);
    }

    private String getFailureUrl() {
        return "/login";
    }
}