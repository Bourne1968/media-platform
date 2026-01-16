package com.dijkstra.aimedia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本生成响应DTO
 * 
 * @author dijkstra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextGenerateResponse {
    
    /**
     * 生成的文本内容
     */
    private String content;
    
    /**
     * 使用的模型
     */
    private String model;
}
