package com.lushan.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private List<String> roles;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String message;
    private boolean success;
}