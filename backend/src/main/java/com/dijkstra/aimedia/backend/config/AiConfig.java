package com.dijkstra.aimedia.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

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
     * AI服务商：aliyun (阿里云DashScope)
     */
    private String provider;
    
    /**
     * 阿里云DashScope配置
     */
    private Aliyun aliyun;
    
    /**
     * OpenAI配置（已废弃，保留用于兼容）
     */
    private OpenAI openai;
    
    /**
     * 百度配置（已废弃，保留用于兼容）
     */
    private Baidu baidu;
    
    /**
     * 代理配置
     */
    private ProxyConfig proxy;
    
    @Data
    public static class Aliyun {
        private String apiKey;
        private String model;
        private String imageModel;
        private String videoModel;
    }
    
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
    
    @Data
    public static class ProxyConfig {
        private String host;
        private Integer port;
    }
    
    /**
     * 配置RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(60000);
        
        if (proxy != null && proxy.getHost() != null && proxy.getPort() != null) {
            Proxy javaProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
            factory.setProxy(javaProxy);
        }
        
        return new RestTemplate(factory);
    }
}
