package com.lushan.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "gardens")
public class Garden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "名称不能为空")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    private String contactInfo;

    @Column(nullable = false)
    private String category; // 类别：温室花园、庐山植物园本部、分部园区

    private Integer sortOrder; // 排序顺序

    private Boolean isPublished = true; // 是否发布

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;

    // 多语言支持
    private String nameEn; // 英文名称
    
    @Column(length = 1000)
    private String descriptionEn; // 英文描述

    private String contactInfoEn; // 英文联系方式
}