package com.dijkstra.aimedia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 图片生成请求DTO
 * 
 * @author dijkstra
 */
@Data
public class ImageGenerateRequest {
    
    @NotBlank(message = "提示词不能为空")
    private String prompt;
    
    /**
     * 图片尺寸，例如：1024x1024, 512x512
     */
    private String size = "1024x1024";
    
    /**
     * 生成数量
     */
    private Integer n = 1;
}
