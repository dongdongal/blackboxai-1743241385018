package com.lushan.repository;

import com.lushan.entity.Garden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GardenRepository extends JpaRepository<Garden, Long> {
    
    // 根据类别查找已发布的花园，按排序顺序排序
    List<Garden> findByIsPublishedTrueAndCategoryOrderBySortOrderAsc(String category);
    
    // 分页查询已发布的花园
    Page<Garden> findByIsPublishedTrue(Pageable pageable);
    
    // 搜索花园（支持中英文）
    @Query("SELECT g FROM Garden g WHERE g.isPublished = true AND " +
           "(LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.descriptionEn) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Garden> searchGardens(@Param("keyword") String keyword, Pageable pageable);
    
    // 获取所有类别
    @Query("SELECT DISTINCT g.category FROM Garden g WHERE g.isPublished = true")
    List<String> findAllCategories();
    
    // 根据多个ID查询花园
    List<Garden> findByIdIn(List<Long> ids);
}