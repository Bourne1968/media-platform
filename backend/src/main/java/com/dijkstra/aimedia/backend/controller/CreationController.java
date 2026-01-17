package com.dijkstra.aimedia.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dijkstra.aimedia.backend.common.Result;
import com.dijkstra.aimedia.backend.constant.UserRole;
import com.dijkstra.aimedia.backend.dto.CreationRecordRequest;
import com.dijkstra.aimedia.backend.dto.CreationRecordResponse;
import com.dijkstra.aimedia.backend.dto.StatisticsResponse;
import com.dijkstra.aimedia.backend.service.CreationService;
import com.dijkstra.aimedia.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 创作记录控制器
 * 
 * @author dijkstra
 */
@RestController
@RequestMapping("/creation")
@RequiredArgsConstructor
public class CreationController {
    
    private final CreationService creationService;
    private final UserService userService;
    
    /**
     * 创建创作记录
     * 
     * @param request 创作记录请求
     * @param autoGenerate 是否自动生成内容（可选，默认false）
     * @param httpRequest HTTP请求（用于获取用户信息）
     * @return 创作记录响应
     */
    @PostMapping
    public Result<CreationRecordResponse> create(
            @Valid @RequestBody CreationRecordRequest request,
            @RequestParam(required = false, defaultValue = "false") Boolean autoGenerate,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        CreationRecordResponse response = creationService.create(userId, request, autoGenerate);
        return Result.success("创建成功", response);
    }
    
    /**
     * 根据ID查询创作记录
     * 
     * @param id 记录ID
     * @param httpRequest HTTP请求（用于获取用户信息）
     * @return 创作记录响应
     */
    @GetMapping("/{id}")
    public Result<CreationRecordResponse> getById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        boolean isAdmin = UserRole.ADMIN.equals(role);
        
        CreationRecordResponse response = creationService.getById(id, userId, isAdmin);
        return Result.success(response);
    }
    
    /**
     * 分页查询创作记录
     * 
     * @param userId 用户ID（可选，管理员可筛选）
     * @param type 类型（可选）
     * @param current 当前页（默认1）
     * @param size 每页数量（默认10）
     * @param httpRequest HTTP请求（用于获取用户信息）
     * @return 分页结果
     */
    @GetMapping
    public Result<IPage<CreationRecordResponse>> getList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        boolean isAdmin = UserRole.ADMIN.equals(role);
        
        // 对于普通用户，使用当前登录用户的ID；管理员可以使用传入的userId参数
        Long queryUserId = isAdmin ? userId : currentUserId;
        
        IPage<CreationRecordResponse> page = creationService.getList(queryUserId, type, keyword, current, size, isAdmin);
        return Result.success(page);
    }
    
    /**
     * 更新创作记录
     * 
     * @param id 记录ID
     * @param request 创作记录请求
     * @param httpRequest HTTP请求（用于获取用户信息）
     * @return 创作记录响应
     */
    @PutMapping("/{id}")
    public Result<CreationRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreationRecordRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        boolean isAdmin = UserRole.ADMIN.equals(role);
        
        CreationRecordResponse response = creationService.update(id, userId, request, isAdmin);
        return Result.success("更新成功", response);
    }
    
    /**
     * 删除创作记录
     * 
     * @param id 记录ID
     * @param httpRequest HTTP请求（用于获取用户信息）
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        boolean isAdmin = UserRole.ADMIN.equals(role);
        
        creationService.delete(id, userId, isAdmin);
        return Result.success("删除成功");
    }
    
    /**
     * 获取统计数据（管理员功能）
     * 
     * @param httpRequest HTTP请求
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<StatisticsResponse> getStatistics(HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!UserRole.ADMIN.equals(role)) {
            return Result.error(403, "权限不足");
        }
        StatisticsResponse statistics = creationService.getStatistics();
        // 添加用户统计
        statistics.setTotalUsers(userService.getTotalUsersCount());
        statistics.setTodayNewUsers(userService.getTodayNewUsersCount());
        return Result.success(statistics);
    }
}
