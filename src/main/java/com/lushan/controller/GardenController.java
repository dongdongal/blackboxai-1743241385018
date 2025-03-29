package com.lushan.controller;

import com.lushan.entity.Garden;
import com.lushan.service.GardenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/garden")
@RequiredArgsConstructor
public class GardenController {
    private final GardenService gardenService;

    // 前台展示页面
    @GetMapping
    public String list(Model model, 
                      @RequestParam(defaultValue = "") String category,
                      @RequestParam(defaultValue = "") String keyword,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "12") int size) {
        
        // 获取所有类别
        List<String> categories = gardenService.getAllCategories();
        model.addAttribute("categories", categories);
        
        // 分页查询
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        
        // 根据条件查询
        Page<Garden> gardens;
        if (!keyword.isEmpty()) {
            gardens = gardenService.searchGardens(keyword, pageRequest);
        } else if (!category.isEmpty()) {
            List<Garden> gardenList = gardenService.findByCategory(category);
            gardens = new org.springframework.data.domain.PageImpl<>(gardenList);
        } else {
            gardens = gardenService.findAllPublished(pageRequest);
        }
        
        model.addAttribute("gardens", gardens);
        model.addAttribute("currentCategory", category);
        model.addAttribute("keyword", keyword);
        
        return "garden/list";
    }

    // 详情页
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Garden garden = gardenService.findById(id);
        model.addAttribute("garden", garden);
        return "garden/detail";
    }

    // 后台管理页面
    @GetMapping("/admin")
    public String adminList(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        Page<Garden> gardens = gardenService.findAllPublished(pageRequest);
        model.addAttribute("gardens", gardens);
        return "garden/admin/list";
    }

    // 新增/编辑表单页
    @GetMapping("/admin/form")
    public String form(@RequestParam(required = false) Long id, Model model) {
        Garden garden = id != null ? gardenService.findById(id) : new Garden();
        model.addAttribute("garden", garden);
        model.addAttribute("categories", gardenService.getAllCategories());
        return "garden/admin/form";
    }

    // 保存
    @PostMapping("/admin/save")
    public String save(@Valid Garden garden,
                      BindingResult result,
                      @RequestParam(required = false) MultipartFile imageFile,
                      RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "garden/admin/form";
        }
        
        try {
            gardenService.save(garden, imageFile);
            redirectAttributes.addFlashAttribute("success", "保存成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "保存失败：" + e.getMessage());
        }
        
        return "redirect:/garden/admin";
    }

    // 删除
    @PostMapping("/admin/delete/{id}")
    @ResponseBody
    public String delete(@PathVariable Long id) {
        try {
            gardenService.delete(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // 更新发布状态
    @PostMapping("/admin/publish/{id}")
    @ResponseBody
    public String updatePublishStatus(@PathVariable Long id,
                                    @RequestParam boolean isPublished) {
        try {
            gardenService.updatePublishStatus(id, isPublished);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // 更新排序
    @PostMapping("/admin/sort/{id}")
    @ResponseBody
    public String updateSortOrder(@PathVariable Long id,
                                @RequestParam Integer sortOrder) {
        try {
            gardenService.updateSortOrder(id, sortOrder);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}