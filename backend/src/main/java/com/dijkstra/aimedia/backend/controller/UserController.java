package com.dijkstra.aimedia.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dijkstra.aimedia.backend.common.Result;
import com.dijkstra.aimedia.backend.constant.UserRole;
import com.dijkstra.aimedia.backend.dto.*;
import com.dijkstra.aimedia.backend.entity.User;
import com.dijkstra.aimedia.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 
 * @author dijkstra
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 登录响应
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = userService.register(request);
        return Result.success("注册成功", response);
    }
    
    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }
    
    /**
     * 获取用户列表（管理员功能）
     * 
     * @param current 当前页
     * @param size 每页数量
     * @param httpRequest HTTP请求
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!UserRole.ADMIN.equals(role)) {
            return Result.error(403, "权限不足");
        }
        IPage<User> page = userService.getUserList(current, size);
        return Result.success(page);
    }
    
    /**
     * 更新用户信息
     * 
     * @param request 更新请求
     * @param httpRequest HTTP请求
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public Result<User> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        User user = userService.updateProfile(userId, request.getUsername());
        return Result.success("更新成功", user);
    }
    
    /**
     * 更新用户头像
     * 
     * @param request 更新请求
     * @param httpRequest HTTP请求
     * @return 更新后的用户信息
     */
    @PutMapping("/avatar")
    public Result<User> updateAvatar(
            @Valid @RequestBody UpdateAvatarRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        User user = userService.updateAvatar(userId, request.getAvatar());
        return Result.success("头像更新成功", user);
    }
    
    /**
     * 修改密码
     * 
     * @param request 修改密码请求
     * @param httpRequest HTTP请求
     * @return 是否成功
     */
    @PutMapping("/password")
    public Result<Boolean> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        boolean success = userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return Result.success("密码修改成功", success);
    }
    
    /**
     * 更新用户偏好设置
     * 
     * @param request 更新偏好设置请求
     * @param httpRequest HTTP请求
     * @return 更新后的用户信息
     */
    @PutMapping("/preferences")
    public Result<User> updatePreferences(
            @Valid @RequestBody UpdatePreferencesRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        User user = userService.updatePreferences(userId, request.getPreferences());
        return Result.success("偏好设置保存成功", user);
    }
    
    /**
     * 获取用户偏好设置
     * 
     * @param httpRequest HTTP请求
     * @return 偏好设置
     */
    @GetMapping("/preferences")
    public Result<java.util.Map<String, Object>> getPreferences(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        java.util.Map<String, Object> preferences = userService.getPreferences(userId);
        return Result.success(preferences);
    }
}
