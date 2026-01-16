package com.dijkstra.aimedia.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创作记录响应DTO
 * 
 * @author dijkstra
 */
@Data
public class CreationRecordResponse {
    
    private Long id;
    
    private Long userId;
    
    private String type;
    
    private String prompt;
    
    private String resultContent;
    
    private String imageUrl;
    
    private String styleTemplate;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
