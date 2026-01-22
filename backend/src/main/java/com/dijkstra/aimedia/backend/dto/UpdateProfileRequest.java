package com.dijkstra.aimedia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新用户信息请求DTO
 * 
 * @author dijkstra
 */
@Data
public class UpdateProfileRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 性别：male, female, other, private
     */
    private String gender;
}
