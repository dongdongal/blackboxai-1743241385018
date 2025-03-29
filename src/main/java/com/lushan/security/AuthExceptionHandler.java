package com.lushan.security;

import com.lushan.security.AuthResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AuthResponse handleUsernameNotFound(UsernameNotFoundException e) {
        return AuthResponse.builder()
                .success(false)
                .message("用户不存在")
                .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AuthResponse handleBadCredentials(BadCredentialsException e) {
        return AuthResponse.builder()
                .success(false)
                .message("用户名或密码错误")
                .build();
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public AuthResponse handleDisabledAccount(DisabledException e) {
        return AuthResponse.builder()
                .success(false)
                .message("账号已被禁用")
                .build();
    }

    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public AuthResponse handleLockedAccount(LockedException e) {
        return AuthResponse.builder()
                .success(false)
                .message("账号已被锁定")
                .build();
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AuthResponse handleJwtException(JwtException e) {
        return AuthResponse.builder()
                .success(false)
                .message("无效的token: " + e.getMessage())
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public AuthResponse handleAccessDenied(AccessDeniedException e) {
        return AuthResponse.builder()
                .success(false)
                .message("没有权限访问此资源")
                .build();
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AuthResponse handleAuthenticationException(AuthenticationException e) {
        return AuthResponse.builder()
                .success(false)
                .message("认证失败: " + e.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleValidationExceptions(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return AuthResponse.builder()
                .success(false)
                .message("验证失败: " + errors)
                .build();
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleBindException(BindException e) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return AuthResponse.builder()
                .success(false)
                .message("绑定失败: " + errors)
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleConstraintViolation(ConstraintViolationException e) {
        return AuthResponse.builder()
                .success(false)
                .message("验证失败: " + e.getMessage())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleIllegalArgument(IllegalArgumentException e) {
        return AuthResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleIllegalState(IllegalStateException e) {
        return AuthResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AuthResponse handleAllOtherExceptions(Exception e) {
        return AuthResponse.builder()
                .success(false)
                .message("服务器内部错误")
                .build();
    }
}