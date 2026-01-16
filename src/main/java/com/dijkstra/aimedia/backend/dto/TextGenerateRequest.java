package com.dijkstra.aimedia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文本生成请求DTO
 * 
 * @author dijkstra
 */
@Data
public class TextGenerateRequest {
    
    @NotBlank(message = "提示词不能为空")
    private String prompt;
    
    private String style;
}
