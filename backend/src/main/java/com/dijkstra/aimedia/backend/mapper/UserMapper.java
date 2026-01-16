package com.dijkstra.aimedia.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dijkstra.aimedia.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * 
 * @author dijkstra
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

