package com.lushan.service;

import com.lushan.entity.Garden;
import com.lushan.exception.BusinessException;
import com.lushan.repository.GardenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GardenService {
    private final GardenRepository gardenRepository;
    
    private static final String UPLOAD_DIR = "uploads/gardens/";

    // 获取所有已发布的花园（分页）
    public Page<Garden> findAllPublished(Pageable pageable) {
        return gardenRepository.findByIsPublishedTrue(pageable);
    }

    // 根据类别获取花园列表
    public List<Garden> findByCategory(String category) {
        return gardenRepository.findByIsPublishedTrueAndCategoryOrderBySortOrderAsc(category);
    }

    // 获取所有类别
    public List<String> getAllCategories() {
        return gardenRepository.findAllCategories();
    }

    // 搜索花园
    public Page<Garden> searchGardens(String keyword, Pageable pageable) {
        return gardenRepository.searchGardens(keyword, pageable);
    }

    // 获取单个花园详情
    public Garden findById(Long id) {
        return gardenRepository.findById(id)
                .orElseThrow(() -> new BusinessException("花园信息不存在"));
    }

    // 保存或更新花园信息
    @Transactional
    public Garden save(Garden garden, MultipartFile imageFile) {
        // 处理图片上传
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = uploadImage(imageFile);
            garden.setImageUrl(imageUrl);
        }
        
        // 如果是新建，设置默认排序顺序
        if (garden.getId() == null && garden.getSortOrder() == null) {
            garden.setSortOrder(1000); // 默认排序值
        }

        return gardenRepository.save(garden);
    }

    // 删除花园信息
    @Transactional
    public void delete(Long id) {
        Garden garden = findById(id);
        // 如果存在图片，删除图片文件
        if (StringUtils.hasText(garden.getImageUrl())) {
            deleteImage(garden.getImageUrl());
        }
        gardenRepository.delete(garden);
    }

    // 更新发布状态
    @Transactional
    public void updatePublishStatus(Long id, boolean isPublished) {
        Garden garden = findById(id);
        garden.setIsPublished(isPublished);
        gardenRepository.save(garden);
    }

    // 更新排序顺序
    @Transactional
    public void updateSortOrder(Long id, Integer sortOrder) {
        Garden garden = findById(id);
        garden.setSortOrder(sortOrder);
        gardenRepository.save(garden);
    }

    // 处理图片上传
    private String uploadImage(MultipartFile file) {
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            return UPLOAD_DIR + filename;
        } catch (IOException e) {
            throw new BusinessException("图片上传失败", e);
        }
    }

    // 删除图片文件
    private void deleteImage(String imageUrl) {
        try {
            Path imagePath = Paths.get(imageUrl);
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            // 记录日志但不抛出异常，因为这不是核心业务
            e.printStackTrace();
        }
    }
}