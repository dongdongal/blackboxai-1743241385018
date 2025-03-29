package com.lushan.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Column(nullable = false)
    private String password;

    @Email(message = "邮箱格式不正确")
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Column(unique = true)
    private String phone;

    private String nickname;

    private String avatar;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean accountNonExpired = true;

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    private boolean credentialsNonExpired = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    // 会员相关字段
    private String membershipType; // NONE, MONTHLY, QUARTERLY, YEARLY
    private LocalDateTime membershipExpireDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;

    // 最后登录时间
    private LocalDateTime lastLoginTime;

    // 登录IP
    private String lastLoginIp;

    // 用于密码重置的token
    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpireTime;

    // 用于邮箱验证的token
    private String emailVerificationToken;
    private boolean emailVerified = false;

    // 用于手机验证的token
    private String phoneVerificationToken;
    private boolean phoneVerified = false;

    // 辅助方法
    public void addRole(String role) {
        roles.add(role);
    }

    public void removeRole(String role) {
        roles.remove(role);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isMembershipValid() {
        return membershipExpireDate != null && 
               membershipExpireDate.isAfter(LocalDateTime.now());
    }

    public void updateLastLogin(String ip) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
    }
}