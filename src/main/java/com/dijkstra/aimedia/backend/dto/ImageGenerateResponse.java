package com.dijkstra.aimedia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图片生成响应DTO
 * 
 * @author dijkstra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerateResponse {
    
    /**
     * 生成的图片URL列表
     */
    private List<String> imageUrls;
    
    /**
     * 使用的模型
     */
    private String model;
}
