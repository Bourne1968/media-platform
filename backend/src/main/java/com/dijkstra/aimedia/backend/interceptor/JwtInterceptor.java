package com.dijkstra.aimedia.backend.interceptor;

import com.dijkstra.aimedia.backend.common.ResultCode;
import com.dijkstra.aimedia.backend.exception.BusinessException;
import com.dijkstra.aimedia.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 * 
 * @author dijkstra
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    
    /**
     * Token在请求头中的字段名
     */
    private static final String TOKEN_HEADER = "Authorization";
    
    /**
     * Token前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 处理OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        // 获取请求路径
        String path = request.getRequestURI();
        
        // 排除登录和注册接口
        if (path.contains("/user/login") || path.contains("/user/register")) {
            return true;
        }
        
        // 获取Token
        String token = getTokenFromRequest(request);
        
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        
        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        
        // 将用户信息存入request，供Controller使用
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        
        return true;
    }
    
    /**
     * 从请求中获取Token
     * 
     * @param request HTTP请求
     * @return Token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
