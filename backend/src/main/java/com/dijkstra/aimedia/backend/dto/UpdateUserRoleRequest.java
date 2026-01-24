package com.dijkstra.aimedia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员更新用户角色请求 DTO
 */
@Data
public class UpdateUserRoleRequest {

    /**
     * 新角色：ADMIN / USER
     */
    @NotBlank(message = "角色不能为空")
    private String role;
}

