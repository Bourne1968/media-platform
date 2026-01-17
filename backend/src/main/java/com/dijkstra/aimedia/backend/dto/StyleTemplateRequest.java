package com.dijkstra.aimedia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 风格模板请求DTO
 * 
 * @author dijkstra
 */
@Data
public class StyleTemplateRequest {
    
    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String name;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 模板提示词
     */
    private String prompt;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 排序
     */
    private Integer sort;
}
