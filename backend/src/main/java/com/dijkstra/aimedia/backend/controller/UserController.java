package com.dijkstra.aimedia.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dijkstra.aimedia.backend.common.Result;
import com.dijkstra.aimedia.backend.constant.UserRole;
import com.dijkstra.aimedia.backend.dto.LoginRequest;
import com.dijkstra.aimedia.backend.dto.LoginResponse;
import com.dijkstra.aimedia.backend.dto.RegisterRequest;
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
}
