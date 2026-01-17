package com.dijkstra.aimedia.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dijkstra.aimedia.backend.common.ResultCode;
import com.dijkstra.aimedia.backend.dto.StyleTemplateRequest;
import com.dijkstra.aimedia.backend.dto.StyleTemplateResponse;
import com.dijkstra.aimedia.backend.entity.StyleTemplate;
import com.dijkstra.aimedia.backend.exception.BusinessException;
import com.dijkstra.aimedia.backend.mapper.StyleTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 风格模板服务类
 * 
 * @author dijkstra
 */
@Service
@RequiredArgsConstructor
public class StyleTemplateService {
    
    private final StyleTemplateMapper styleTemplateMapper;
    
    /**
     * 创建风格模板
     */
    @Transactional(rollbackFor = Exception.class)
    public StyleTemplateResponse create(StyleTemplateRequest request) {
        StyleTemplate template = new StyleTemplate();
        BeanUtils.copyProperties(request, template);
        template.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        template.setSort(request.getSort() != null ? request.getSort() : 0);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        
        styleTemplateMapper.insert(template);
        
        return convertToResponse(template);
    }
    
    /**
     * 更新风格模板
     */
    @Transactional(rollbackFor = Exception.class)
    public StyleTemplateResponse update(Long id, StyleTemplateRequest request) {
        StyleTemplate template = styleTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "模板不存在");
        }
        
        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getPrompt() != null) {
            template.setPrompt(request.getPrompt());
        }
        if (request.getEnabled() != null) {
            template.setEnabled(request.getEnabled());
        }
        if (request.getSort() != null) {
            template.setSort(request.getSort());
        }
        template.setUpdateTime(LocalDateTime.now());
        
        styleTemplateMapper.updateById(template);
        
        return convertToResponse(template);
    }
    
    /**
     * 删除风格模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        styleTemplateMapper.deleteById(id);
    }
    
    /**
     * 根据ID查询模板
     */
    public StyleTemplateResponse getById(Long id) {
        StyleTemplate template = styleTemplateMapper.selectById(id);
        if (template == null) {
            return null;
        }
        return convertToResponse(template);
    }
    
    /**
     * 获取所有启用的模板（用于前端选择）
     */
    public List<StyleTemplateResponse> getEnabledTemplates() {
        LambdaQueryWrapper<StyleTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StyleTemplate::getEnabled, true);
        queryWrapper.orderByAsc(StyleTemplate::getSort);
        queryWrapper.orderByDesc(StyleTemplate::getCreateTime);
        
        List<StyleTemplate> templates = styleTemplateMapper.selectList(queryWrapper);
        return templates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有模板（管理员）
     */
    public List<StyleTemplateResponse> getAllTemplates() {
        LambdaQueryWrapper<StyleTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(StyleTemplate::getSort);
        queryWrapper.orderByDesc(StyleTemplate::getCreateTime);
        
        List<StyleTemplate> templates = styleTemplateMapper.selectList(queryWrapper);
        return templates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 实体转换为响应DTO
     */
    private StyleTemplateResponse convertToResponse(StyleTemplate template) {
        if (template == null) {
            return null;
        }
        StyleTemplateResponse response = new StyleTemplateResponse();
        BeanUtils.copyProperties(template, response);
        return response;
    }
}
