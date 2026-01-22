package com.dijkstra.aimedia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新头像请求DTO
 * 
 * @author dijkstra
 */
@Data
public class UpdateAvatarRequest {
    
    /**
     * 头像（base64或URL）
     */
    @NotBlank(message = "头像不能为空")
    private String avatar;
}
