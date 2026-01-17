package com.dijkstra.aimedia.backend.controller;

import com.dijkstra.aimedia.backend.common.Result;
import com.dijkstra.aimedia.backend.constant.UserRole;
import com.dijkstra.aimedia.backend.dto.StyleTemplateRequest;
import com.dijkstra.aimedia.backend.dto.StyleTemplateResponse;
import com.dijkstra.aimedia.backend.service.StyleTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 风格模板控制器
 * 
 * @author dijkstra
 */
@RestController
@RequestMapping("/style-template")
@RequiredArgsConstructor
public class StyleTemplateController {
    
    private final StyleTemplateService styleTemplateService;
    
    /**
     * 获取所有启用的模板（公开接口，用于前端选择）
     */
    @GetMapping("/enabled")
    public Result<List<StyleTemplateResponse>> getEnabledTemplates() {
        List<StyleTemplateResponse> templates = styleTemplateService.getEnabledTemplates();
        return Result.success(templates);
    }
    
    /**
     * 获取所有模板（管理员）
     */
    @GetMapping
    public Result<List<StyleTemplateResponse>> getAllTemplates(HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!UserRole.ADMIN.equals(role)) {
            return Result.error(403, "权限不足");
        }
        List<StyleTemplateResponse> templates = styleTemplateService.getAllTemplates();
        return Result.success(templates);
    }
    
    /**
     * 创建模板（管理员）
     */
    @PostMapping
    public Result<StyleTemplateResponse> create(
            @Valid @RequestBody StyleTemplateRequest request,
            HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!UserRole.ADMIN.equals(role)) {
            return Result.error(403, "权限不足");
        }
        StyleTemplateResponse response = styleTemplateService.create(request);
        return Result.success("创建成功", response);
    }
    
    /**
     * 更新模板（管理员）
     */
    @PutMapping("/{id}")
    public Result<StyleTemplateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StyleTemplateRequest request,
            HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!UserRole.ADMIN.equals(role)) {
            return Result.error(403, "权限不足");
        }
        StyleTemplateResponse response = styleTemplateService.update(id, request);
        return Result.success("更新成功", response);
    }
    
    /**
     * 删除模板（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!UserRole.ADMIN.equals(role)) {
            return Result.error(403, "权限不足");
        }
        styleTemplateService.delete(id);
        return Result.success("删除成功");
    }
}
