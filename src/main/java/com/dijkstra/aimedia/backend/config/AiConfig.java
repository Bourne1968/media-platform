package com.dijkstra.aimedia.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI配置类
 * 
 * @author dijkstra
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    
    /**
     * AI服务商：openai, baidu
     */
    private String provider;
    
    /**
     * OpenAI配置
     */
    private OpenAI openai;
    
    /**
     * 百度配置
     */
    private Baidu baidu;
    
    @Data
    public static class OpenAI {
        private String apiKey;
        private String baseUrl;
        private String model;
        private String imageModel;
    }
    
    @Data
    public static class Baidu {
        private String apiKey;
        private String secretKey;
        private String accessTokenUrl;
        private String chatUrl;
        private String imageUrl;
    }
    
    /**
     * 配置RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        ((SimpleClientHttpRequestFactory) factory).setConnectTimeout(30000);
        ((SimpleClientHttpRequestFactory) factory).setReadTimeout(60000);
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
}
