package com.lushan.repository;

import com.lushan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    Optional<User> findByResetPasswordToken(String token);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByPhoneVerificationToken(String token);

    @Query("SELECT u FROM User u WHERE u.membershipExpireDate < :now")
    List<User> findExpiredMemberships(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginTime = :time, u.lastLoginIp = :ip WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("time") LocalDateTime time, @Param("ip") String ip);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void verifyEmail(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.phoneVerified = true WHERE u.id = :userId")
    void verifyPhone(@Param("userId") Long userId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createTime >= :startTime AND u.createTime < :endTime")
    long countNewUsersInPeriod(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT u FROM User u WHERE u.membershipType = :type AND u.membershipExpireDate > :now")
    List<User> findActiveUsersByMembershipType(@Param("type") String type, @Param("now") LocalDateTime now);
}