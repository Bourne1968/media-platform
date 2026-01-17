package com.dijkstra.aimedia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 统计数据响应DTO
 * 
 * @author dijkstra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    
    /**
     * 总用户数
     */
    private Long totalUsers;
    
    /**
     * 总创作记录数
     */
    private Long totalRecords;
    
    /**
     * 文本创作数
     */
    private Long textRecords;
    
    /**
     * 图像创作数
     */
    private Long imageRecords;
    
    /**
     * 今日新增用户数
     */
    private Long todayNewUsers;
    
    /**
     * 今日新增记录数
     */
    private Long todayNewRecords;
    
    /**
     * 按类型统计
     */
    private Map<String, Long> typeStatistics;
    
    /**
     * 按日期统计（最近7天）
     */
    private Map<String, Long> dailyStatistics;
}
