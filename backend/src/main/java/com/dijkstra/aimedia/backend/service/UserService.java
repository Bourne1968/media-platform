package com.dijkstra.aimedia.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dijkstra.aimedia.backend.common.ResultCode;
import com.dijkstra.aimedia.backend.constant.UserRole;
import com.dijkstra.aimedia.backend.dto.LoginRequest;
import com.dijkstra.aimedia.backend.dto.LoginResponse;
import com.dijkstra.aimedia.backend.dto.RegisterRequest;
import com.dijkstra.aimedia.backend.entity.User;
import com.dijkstra.aimedia.backend.exception.BusinessException;
import com.dijkstra.aimedia.backend.mapper.UserMapper;
import com.dijkstra.aimedia.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务类
 * 
 * @author dijkstra
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 登录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        // 密码加密（实际项目中应使用BCrypt等加密算法）
        user.setPassword(request.getPassword()); // TODO: 使用BCrypt加密
        user.setRole(UserRole.USER); // 默认普通用户
        user.setAvatar(request.getAvatar());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 返回登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        
        return response;
    }
    
    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }
        
        // 验证密码（实际项目中应使用BCrypt等加密算法验证）
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 返回登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        
        return response;
    }
    
    /**
     * 根据ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}
