package com.dijkstra.aimedia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创作记录请求DTO
 * 
 * @author dijkstra
 */
@Data
public class CreationRecordRequest {
    
    @NotBlank(message = "类型不能为空")
    private String type;
    
    @NotBlank(message = "提示词不能为空")
    private String prompt;
    
    private String resultContent;
    
    private String imageUrl;
    
    private String styleTemplate;
}
