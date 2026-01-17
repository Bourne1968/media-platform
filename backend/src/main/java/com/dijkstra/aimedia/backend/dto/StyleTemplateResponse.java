package com.dijkstra.aimedia.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风格模板响应DTO
 * 
 * @author dijkstra
 */
@Data
public class StyleTemplateResponse {
    
    private Long id;
    private String name;
    private String description;
    private String prompt;
    private Boolean enabled;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
