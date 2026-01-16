package com.dijkstra.aimedia.backend.common;

import lombok.Getter;

/**
 * 响应码枚举
 * 
 * @author dijkstra
 */
@Getter
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 失败
     */
    ERROR(500, "操作失败"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),
    
    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(1001, "用户名已存在"),
    
    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(1002, "用户名或密码错误"),
    
    /**
     * Token无效或已过期
     */
    TOKEN_INVALID(1003, "Token无效或已过期"),
    
    /**
     * 权限不足
     */
    PERMISSION_DENIED(1004, "权限不足"),
    
    /**
     * 创作记录不存在
     */
    RECORD_NOT_FOUND(2001, "创作记录不存在"),
    
    /**
     * 无权限操作该记录
     */
    RECORD_ACCESS_DENIED(2002, "无权限操作该记录");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
