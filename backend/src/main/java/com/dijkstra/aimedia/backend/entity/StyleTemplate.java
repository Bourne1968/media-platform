package com.dijkstra.aimedia.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风格模板实体类
 * 
 * @author dijkstra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("style_template")
public class StyleTemplate {
    
    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 模板提示词（用于生成内容）
     */
    private String prompt;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
