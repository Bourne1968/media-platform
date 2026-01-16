package com.dijkstra.aimedia.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创作记录实体类
 * 
 * @author dijkstra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("creation_record")
public class CreationRecord {
    
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 类型：TEXT（文本）/IMAGE（图像）
     */
    private String type;
    
    /**
     * 输入提示词
     */
    private String prompt;
    
    /**
     * 生成结果内容（文本类型使用）
     */
    private String resultContent;
    
    /**
     * 图片URL（图像类型使用）
     */
    private String imageUrl;
    
    /**
     * 风格模板
     */
    private String styleTemplate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

