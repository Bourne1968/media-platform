package com.dijkstra.aimedia.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dijkstra.aimedia.backend.common.ResultCode;
import com.dijkstra.aimedia.backend.constant.CreationType;
import com.dijkstra.aimedia.backend.dto.CreationRecordRequest;
import com.dijkstra.aimedia.backend.dto.CreationRecordResponse;
import com.dijkstra.aimedia.backend.dto.StatisticsResponse;
import com.dijkstra.aimedia.backend.entity.CreationRecord;
import com.dijkstra.aimedia.backend.exception.BusinessException;
import com.dijkstra.aimedia.backend.mapper.CreationRecordMapper;
import com.dijkstra.aimedia.backend.dto.TextGenerateResponse;
import com.dijkstra.aimedia.backend.dto.ImageGenerateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 创作记录服务类
 * 
 * @author dijkstra
 */
@Service
@RequiredArgsConstructor
public class CreationService {
    
    private final CreationRecordMapper creationRecordMapper;
    private final AIService aiService;
    
    /**
     * 创建创作记录
     * 
     * @param userId 用户ID
     * @param request 创作记录请求
     * @param autoGenerate 是否自动生成内容（如果为true且内容为空，则调用AI生成）
     * @return 创作记录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public CreationRecordResponse create(Long userId, CreationRecordRequest request, Boolean autoGenerate) {
        // 验证类型
        if (!CreationType.TEXT.equals(request.getType()) && !CreationType.IMAGE.equals(request.getType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "类型必须为TEXT或IMAGE");
        }
        
        CreationRecord record = new CreationRecord();
        BeanUtils.copyProperties(request, record);
        record.setUserId(userId);
        
        // 如果开启自动生成且内容为空，则调用AI生成
        if (Boolean.TRUE.equals(autoGenerate)) {
            if (CreationType.TEXT.equals(request.getType())) {
                // 文本类型：如果resultContent为空，则生成文本
                if (request.getResultContent() == null || request.getResultContent().isEmpty()) {
                    TextGenerateResponse textResponse = aiService.generateText(request.getPrompt(), request.getStyleTemplate());
                    record.setResultContent(textResponse.getContent());
                }
            } else if (CreationType.IMAGE.equals(request.getType())) {
                // 图像类型：如果imageUrl为空，则生成图片
                if (request.getImageUrl() == null || request.getImageUrl().isEmpty()) {
                    ImageGenerateResponse imageResponse = aiService.generateImage(request.getPrompt());
                    if (imageResponse.getImageUrls() != null && !imageResponse.getImageUrls().isEmpty()) {
                        record.setImageUrl(imageResponse.getImageUrls().get(0));
                    }
                }
            }
        }
        
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        
        creationRecordMapper.insert(record);
        
        return convertToResponse(record);
    }
    
    /**
     * 创建创作记录（不自动生成）
     */
    @Transactional(rollbackFor = Exception.class)
    public CreationRecordResponse create(Long userId, CreationRecordRequest request) {
        return create(userId, request, false);
    }
    
    /**
     * 根据ID查询创作记录
     * 
     * @param id 记录ID
     * @param userId 用户ID（用于权限验证）
     * @param isAdmin 是否为管理员
     * @return 创作记录响应
     */
    public CreationRecordResponse getById(Long id, Long userId, boolean isAdmin) {
        CreationRecord record = creationRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }
        
        // 权限验证：非管理员只能查看自己的记录
        if (!isAdmin && !record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.RECORD_ACCESS_DENIED);
        }
        
        return convertToResponse(record);
    }
    
    /**
     * 分页查询创作记录
     * 
     * @param userId 用户ID（null表示查询所有，管理员功能）
     * @param type 类型（可选）
     * @param keyword 关键词搜索（可选，搜索提示词或内容）
     * @param current 当前页
     * @param size 每页数量
     * @param isAdmin 是否为管理员
     * @return 分页结果
     */
    public IPage<CreationRecordResponse> getList(Long userId, String type, String keyword, Long current, Long size, boolean isAdmin) {
        Page<CreationRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<CreationRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        // 非管理员只能查看自己的记录
        if (!isAdmin) {
            queryWrapper.eq(CreationRecord::getUserId, userId);
        } else if (userId != null) {
            // 管理员可以按用户ID筛选
            queryWrapper.eq(CreationRecord::getUserId, userId);
        }
        
        // 按类型筛选
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(CreationRecord::getType, type);
        }
        
        // 关键词搜索（搜索提示词或内容）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like(CreationRecord::getPrompt, keyword)
                .or()
                .like(CreationRecord::getResultContent, keyword)
            );
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(CreationRecord::getCreateTime);
        
        IPage<CreationRecord> recordPage = creationRecordMapper.selectPage(page, queryWrapper);
        
        // 转换为响应DTO
        IPage<CreationRecordResponse> responsePage = new Page<>(current, size, recordPage.getTotal());
        List<CreationRecordResponse> records = recordPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(records);
        
        return responsePage;
    }
    
    /**
     * 更新创作记录
     * 
     * @param id 记录ID
     * @param userId 用户ID
     * @param request 创作记录请求
     * @param isAdmin 是否为管理员
     * @return 创作记录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public CreationRecordResponse update(Long id, Long userId, CreationRecordRequest request, boolean isAdmin) {
        CreationRecord record = creationRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }
        
        // 权限验证：非管理员只能更新自己的记录
        if (!isAdmin && !record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.RECORD_ACCESS_DENIED);
        }
        
        // 验证类型
        if (request.getType() != null 
                && !CreationType.TEXT.equals(request.getType()) 
                && !CreationType.IMAGE.equals(request.getType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "类型必须为TEXT或IMAGE");
        }
        
        // 更新字段
        if (request.getType() != null) {
            record.setType(request.getType());
        }
        if (request.getPrompt() != null) {
            record.setPrompt(request.getPrompt());
        }
        if (request.getResultContent() != null) {
            record.setResultContent(request.getResultContent());
        }
        if (request.getImageUrl() != null) {
            record.setImageUrl(request.getImageUrl());
        }
        if (request.getStyleTemplate() != null) {
            record.setStyleTemplate(request.getStyleTemplate());
        }
        record.setUpdateTime(LocalDateTime.now());
        
        creationRecordMapper.updateById(record);
        
        return convertToResponse(record);
    }
    
    /**
     * 删除创作记录
     * 
     * @param id 记录ID
     * @param userId 用户ID
     * @param isAdmin 是否为管理员
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId, boolean isAdmin) {
        CreationRecord record = creationRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }
        
        // 权限验证：非管理员只能删除自己的记录
        if (!isAdmin && !record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.RECORD_ACCESS_DENIED);
        }
        
        creationRecordMapper.deleteById(id);
    }
    
    /**
     * 实体转换为响应DTO
     */
    private CreationRecordResponse convertToResponse(CreationRecord record) {
        CreationRecordResponse response = new CreationRecordResponse();
        BeanUtils.copyProperties(record, response);
        return response;
    }
    
    /**
     * 获取统计数据（管理员功能）
     * 
     * @return 统计数据
     */
    public StatisticsResponse getStatistics() {
        StatisticsResponse response = new StatisticsResponse();
        
        // 总记录数
        long totalRecords = creationRecordMapper.selectCount(null);
        response.setTotalRecords(totalRecords);
        
        // 文本记录数
        LambdaQueryWrapper<CreationRecord> textWrapper = new LambdaQueryWrapper<>();
        textWrapper.eq(CreationRecord::getType, CreationType.TEXT);
        long textRecords = creationRecordMapper.selectCount(textWrapper);
        response.setTextRecords(textRecords);
        
        // 图像记录数
        LambdaQueryWrapper<CreationRecord> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(CreationRecord::getType, CreationType.IMAGE);
        long imageRecords = creationRecordMapper.selectCount(imageWrapper);
        response.setImageRecords(imageRecords);
        
        // 今日新增记录数
        LambdaQueryWrapper<CreationRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(CreationRecord::getCreateTime, LocalDate.now().atStartOfDay());
        long todayNewRecords = creationRecordMapper.selectCount(todayWrapper);
        response.setTodayNewRecords(todayNewRecords);
        
        // 按类型统计
        Map<String, Long> typeStatistics = new HashMap<>();
        typeStatistics.put("TEXT", textRecords);
        typeStatistics.put("IMAGE", imageRecords);
        response.setTypeStatistics(typeStatistics);
        
        // 最近7天统计
        Map<String, Long> dailyStatistics = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();
            
            LambdaQueryWrapper<CreationRecord> dayWrapper = new LambdaQueryWrapper<>();
            dayWrapper.ge(CreationRecord::getCreateTime, startTime);
            dayWrapper.lt(CreationRecord::getCreateTime, endTime);
            long count = creationRecordMapper.selectCount(dayWrapper);
            dailyStatistics.put(date.toString(), count);
        }
        response.setDailyStatistics(dailyStatistics);
        
        return response;
    }
}
