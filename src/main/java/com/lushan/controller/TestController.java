package com.lushan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "庐山杏林中药植物研究所系统运行正常");
        return response;
    }

    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "这是一个公开的测试接口");
        return response;
    }

    @GetMapping("/protected")
    public Map<String, String> protectedEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "这是一个受保护的测试接口，需要认证才能访问");
        return response;
    }

    @GetMapping("/admin")
    public Map<String, String> adminEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "这是一个管理员测试接口，需要ADMIN角色才能访问");
        return response;
    }
}