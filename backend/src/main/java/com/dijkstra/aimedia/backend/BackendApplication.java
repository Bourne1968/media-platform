package com.dijkstra.aimedia.backend;

import com.dijkstra.aimedia.backend.config.AiConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 自媒体AI创作辅助平台后端应用主类
 * 
 * @author dijkstra
 */
@SpringBootApplication
@MapperScan("com.dijkstra.aimedia.backend.mapper")
@EnableConfigurationProperties(AiConfig.class)
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
