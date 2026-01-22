package com.dijkstra.aimedia.backend.dto;

import lombok.Data;

import java.util.Map;

/**
 * 更新用户偏好设置请求DTO
 *
 * @author dijkstra
 */
@Data
public class UpdatePreferencesRequest {

    /**
     * 偏好设置（JSON格式的Map）
     */
    private Map<String, Object> preferences;
}
