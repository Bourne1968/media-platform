package com.dijkstra.aimedia.backend.service;

import com.dijkstra.aimedia.backend.config.AiConfig;
import com.dijkstra.aimedia.backend.dto.ImageGenerateResponse;
import com.dijkstra.aimedia.backend.dto.TextGenerateResponse;
import com.dijkstra.aimedia.backend.exception.BusinessException;
import com.dijkstra.aimedia.backend.common.ResultCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI服务类
 * 支持OpenAI和百度文心一言
 * 
 * @author dijkstra
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    
    private final RestTemplate restTemplate;
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 生成文本内容（文案或标题）
     * 
     * @param prompt 提示词
     * @param style 风格模板
     * @return 生成的文本
     */
    public TextGenerateResponse generateText(String prompt, String style) {
        String provider = aiConfig.getProvider();
        
        if ("openai".equalsIgnoreCase(provider)) {
            return generateTextWithOpenAI(prompt, style);
        } else if ("baidu".equalsIgnoreCase(provider)) {
            return generateTextWithBaidu(prompt, style);
        } else {
            throw new BusinessException(ResultCode.ERROR, "不支持的AI服务商：" + provider);
        }
    }
    
    /**
     * 生成图片
     * 
     * @param prompt 提示词
     * @return 生成的图片URL
     */
    public ImageGenerateResponse generateImage(String prompt) {
        String provider = aiConfig.getProvider();
        
        if ("openai".equalsIgnoreCase(provider)) {
            return generateImageWithOpenAI(prompt);
        } else if ("baidu".equalsIgnoreCase(provider)) {
            return generateImageWithBaidu(prompt);
        } else {
            throw new BusinessException(ResultCode.ERROR, "不支持的AI服务商：" + provider);
        }
    }
    
    /**
     * 使用OpenAI生成文本
     */
    private TextGenerateResponse generateTextWithOpenAI(String prompt, String style) {
        try {
            AiConfig.OpenAI config = aiConfig.getOpenai();
            String url = config.getBaseUrl() + "/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());
            
            // 构建提示词（如果指定了风格，添加到提示词中）
            String fullPrompt = prompt;
            if (style != null && !style.isEmpty()) {
                fullPrompt = String.format("请按照%s风格生成内容：%s", style, prompt);
            }
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "user", "content", fullPrompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String content = jsonNode.get("choices").get(0).get("message").get("content").asText();
            
            return new TextGenerateResponse(content, config.getModel());
        } catch (Exception e) {
            log.error("OpenAI文本生成失败：", e);
            String errorMsg = e.getMessage();
            // 处理 API 余额不足的友好提示
            if (errorMsg != null && errorMsg.contains("Insufficient Balance")) {
                throw new BusinessException(ResultCode.ERROR, "AI API 余额不足，请充值或更换 API Key");
            } else if (errorMsg != null && errorMsg.contains("402")) {
                throw new BusinessException(ResultCode.ERROR, "AI API 账户余额不足，请联系管理员充值");
            }
            throw new BusinessException(ResultCode.ERROR, "文本生成失败：" + errorMsg);
        }
    }
    
    /**
     * 使用百度文心一言生成文本
     */
    private TextGenerateResponse generateTextWithBaidu(String prompt, String style) {
        try {
            AiConfig.Baidu config = aiConfig.getBaidu();
            String accessToken = getBaiduAccessToken();
            String url = config.getChatUrl() + "?access_token=" + accessToken;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 构建提示词（如果指定了风格，添加到提示词中）
            String fullPrompt = prompt;
            if (style != null && !style.isEmpty()) {
                fullPrompt = String.format("请按照%s风格生成内容：%s", style, prompt);
            }
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "user", "content", fullPrompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_output_tokens", 2000);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("error_code")) {
                throw new BusinessException(ResultCode.ERROR, "百度API错误：" + jsonNode.get("error_msg").asText());
            }
            String content = jsonNode.get("result").asText();
            
            return new TextGenerateResponse(content, "baidu-wenxin");
        } catch (Exception e) {
            log.error("百度文本生成失败：", e);
            throw new BusinessException(ResultCode.ERROR, "文本生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 使用OpenAI生成图片
     */
    private ImageGenerateResponse generateImageWithOpenAI(String prompt) {
        try {
            AiConfig.OpenAI config = aiConfig.getOpenai();
            String url = config.getBaseUrl() + "/images/generations";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);
            requestBody.put("n", 1);
            requestBody.put("size", "1024x1024");
            requestBody.put("model", config.getImageModel());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            List<String> imageUrls = new ArrayList<>();
            jsonNode.get("data").forEach(data -> {
                imageUrls.add(data.get("url").asText());
            });
            
            return new ImageGenerateResponse(imageUrls, config.getImageModel());
        } catch (Exception e) {
            log.error("OpenAI图片生成失败：", e);
            throw new BusinessException(ResultCode.ERROR, "图片生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 使用百度生成图片
     */
    private ImageGenerateResponse generateImageWithBaidu(String prompt) {
        try {
            AiConfig.Baidu config = aiConfig.getBaidu();
            String accessToken = getBaiduAccessToken();
            String url = config.getImageUrl() + "?access_token=" + accessToken;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);
            requestBody.put("size", "1024x1024");
            requestBody.put("n", 1);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("error_code")) {
                throw new BusinessException(ResultCode.ERROR, "百度API错误：" + jsonNode.get("error_msg").asText());
            }
            
            List<String> imageUrls = new ArrayList<>();
            if (jsonNode.has("data")) {
                jsonNode.get("data").forEach(data -> {
                    if (data.has("b64_image")) {
                        // 百度返回的是base64图片，这里需要转换为URL或直接返回base64
                        // 实际使用时可能需要上传到OSS或CDN
                        imageUrls.add("data:image/png;base64," + data.get("b64_image").asText());
                    } else if (data.has("image")) {
                        imageUrls.add(data.get("image").asText());
                    }
                });
            }
            
            return new ImageGenerateResponse(imageUrls, "baidu-sd-xl");
        } catch (Exception e) {
            log.error("百度图片生成失败：", e);
            throw new BusinessException(ResultCode.ERROR, "图片生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取百度Access Token（Token有效期30天，建议缓存）
     */
    private String getBaiduAccessToken() {
        try {
            AiConfig.Baidu config = aiConfig.getBaidu();
            String url = config.getAccessTokenUrl() + "?grant_type=client_credentials&client_id=" 
                    + config.getApiKey() + "&client_secret=" + config.getSecretKey();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            if (jsonNode.has("error")) {
                throw new BusinessException(ResultCode.ERROR, "获取百度Token失败：" + jsonNode.get("error_description").asText());
            }
            
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("获取百度Access Token失败：", e);
            throw new BusinessException(ResultCode.ERROR, "获取百度Access Token失败：" + e.getMessage());
        }
    }
}
