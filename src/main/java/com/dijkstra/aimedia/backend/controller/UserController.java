package com.dijkstra.aimedia.backend.controller;

import com.dijkstra.aimedia.backend.common.Result;
import com.dijkstra.aimedia.backend.dto.LoginRequest;
import com.dijkstra.aimedia.backend.dto.LoginResponse;
import com.dijkstra.aimedia.backend.dto.RegisterRequest;
import com.dijkstra.aimedia.backend.service.UserService;
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
}
