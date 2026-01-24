package com.dijkstra.aimedia.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dijkstra.aimedia.backend.common.ResultCode;
import com.dijkstra.aimedia.backend.constant.UserRole;
import com.dijkstra.aimedia.backend.dto.LoginRequest;
import com.dijkstra.aimedia.backend.dto.LoginResponse;
import com.dijkstra.aimedia.backend.dto.RegisterRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dijkstra.aimedia.backend.entity.User;
import com.dijkstra.aimedia.backend.exception.BusinessException;
import com.dijkstra.aimedia.backend.mapper.UserMapper;
import com.dijkstra.aimedia.backend.mapper.CreationRecordMapper;
import com.dijkstra.aimedia.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 用户服务类
 * 
 * @author dijkstra
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final CreationRecordMapper creationRecordMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            LambdaQueryWrapper<User> emailQueryWrapper = new LambdaQueryWrapper<>();
            emailQueryWrapper.eq(User::getEmail, request.getEmail());
            User existingEmailUser = userMapper.selectOne(emailQueryWrapper);
            if (existingEmailUser != null) {
                throw new BusinessException(ResultCode.ERROR.getCode(), "该邮箱已被注册");
            }
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
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
        response.setEmail(user.getEmail());
        
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
        response.setEmail(user.getEmail());
        
        return response;
    }
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateProfile(Long userId, com.dijkstra.aimedia.backend.dto.UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }
        
        // 如果用户名改变，检查是否重复
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, request.getUsername());
            User existingUser = userMapper.selectOne(queryWrapper);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new BusinessException(ResultCode.ERROR.getCode(), "用户名已存在");
            }
            user.setUsername(request.getUsername());
        }
        
        // 将昵称、简介、性别存储在preferences JSON中
        try {
            java.util.Map<String, Object> preferences = new java.util.HashMap<>();
            if (user.getPreferences() != null && !user.getPreferences().isEmpty()) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> existingPrefs = (java.util.Map<String, Object>) objectMapper.readValue(
                    user.getPreferences(), 
                    objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class));
                preferences = existingPrefs;
            }
            
            // 更新个人信息字段
            if (request.getNickname() != null) {
                preferences.put("nickname", request.getNickname());
            }
            if (request.getBio() != null) {
                preferences.put("bio", request.getBio());
            }
            if (request.getGender() != null) {
                preferences.put("gender", request.getGender());
            }
            
            // 保存到preferences字段
            user.setPreferences(objectMapper.writeValueAsString(preferences));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "保存用户信息失败");
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }
    
    /**
     * 更新用户头像
     * 
     * @param userId 用户ID
     * @param avatar 头像（base64或URL）
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateAvatar(Long userId, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }
        
        user.setAvatar(avatar);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }
    
    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }
        
        // 验证当前密码
        if (!user.getPassword().equals(currentPassword)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "当前密码错误");
        }
        
        // 更新密码（实际项目中应使用BCrypt加密）
        user.setPassword(newPassword); // TODO: 使用BCrypt加密
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return true;
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
    
    /**
     * 获取用户列表（管理员功能，可按用户名和角色筛选）
     * 
     * @param current  当前页
     * @param size     每页数量
     * @param username 用户名（可选，支持精确匹配）
     * @param role     角色（可选，ADMIN / USER）
     * @return 分页结果
     */
    public IPage<User> getUserList(Long current, Long size, String username, String role) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            queryWrapper.eq(User::getUsername, username);
        }
        if (role != null && !role.isEmpty()) {
            queryWrapper.eq(User::getRole, role);
        }

        queryWrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 获取今日新增用户数
     * 
     * @return 今日新增用户数
     */
    public Long getTodayNewUsersCount() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(User::getCreateTime, LocalDate.now().atStartOfDay());
        return userMapper.selectCount(queryWrapper);
    }
    
    /**
     * 获取总用户数
     * 
     * @return 总用户数
     */
    public Long getTotalUsersCount() {
        return userMapper.selectCount(null);
    }

    /**
     * 管理员更新用户角色
     *
     * @param userId  用户ID
     * @param newRole 新角色（ADMIN / USER）
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateUserRoleByAdmin(Long userId, String newRole) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }

        if (!UserRole.ADMIN.equals(newRole) && !UserRole.USER.equals(newRole)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "非法角色类型");
        }

        user.setRole(newRole);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }

    /**
     * 管理员批量删除用户（同时删除创作记录）
     *
     * @param userIds 用户ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUsersByAdmin(java.util.List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            // 复用已有的删除逻辑
            deleteAccount(userId);
        }
    }
    
    /**
     * 更新用户偏好设置
     * 
     * @param userId 用户ID
     * @param preferencesMap 偏好设置Map
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public User updatePreferences(Long userId, java.util.Map<String, Object> preferencesMap) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }
        
        try {
            // 将Map转换为JSON字符串
            String preferencesJson = objectMapper.writeValueAsString(preferencesMap);
            user.setPreferences(preferencesJson);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            return user;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "偏好设置格式错误");
        }
    }
    
    /**
     * 获取用户偏好设置
     * 
     * @param userId 用户ID
     * @return 偏好设置Map，如果不存在则返回null
     */
    public java.util.Map<String, Object> getPreferences(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }
        
        if (user.getPreferences() == null || user.getPreferences().isEmpty()) {
            return null;
        }
        
        try {
            // 将JSON字符串转换为Map
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> result = (java.util.Map<String, Object>) objectMapper.readValue(user.getPreferences(), 
                objectMapper.getTypeFactory().constructMapType(
                    java.util.Map.class, String.class, Object.class));
            return result;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "偏好设置格式错误");
        }
    }
    
    /**
     * 删除用户账户（同时删除所有创作记录）
     * 
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }
        
        // 删除用户的所有创作记录
        LambdaQueryWrapper<com.dijkstra.aimedia.backend.entity.CreationRecord> recordWrapper = 
            new LambdaQueryWrapper<>();
        recordWrapper.eq(com.dijkstra.aimedia.backend.entity.CreationRecord::getUserId, userId);
        creationRecordMapper.delete(recordWrapper);
        
        // 删除用户
        userMapper.deleteById(userId);
    }
}
