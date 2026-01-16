package com.dijkstra.aimedia.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dijkstra.aimedia.backend.entity.CreationRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 创作记录 Mapper 接口
 * 
 * @author dijkstra
 */
@Mapper
public interface CreationRecordMapper extends BaseMapper<CreationRecord> {
}

