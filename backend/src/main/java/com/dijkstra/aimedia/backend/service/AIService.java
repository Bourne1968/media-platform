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
 * 使用阿里云DashScope API（通义千问 + 通义万相）
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
     * 使用阿里云DashScope通义千问API
     * 
     * @param prompt 提示词
     * @param style 风格模板
     * @return 生成的文本
     */
    public TextGenerateResponse generateText(String prompt, String style) {
        String provider = aiConfig.getProvider();
        
        if ("aliyun".equalsIgnoreCase(provider)) {
            return generateTextWithAliyun(prompt, style);
        } else {
            throw new BusinessException(ResultCode.ERROR, "不支持的AI服务商：" + provider + "，请使用 aliyun");
        }
    }
    
    /**
     * 生成图片
     * 使用阿里云DashScope通义万相API
     * 
     * @param prompt 提示词
     * @return 生成的图片URL
     */
    public ImageGenerateResponse generateImage(String prompt) {
        return generateImage(prompt, "1024*1024");
    }
    
    public ImageGenerateResponse generateImage(String prompt, String size) {
        String provider = aiConfig.getProvider();
        
        if ("aliyun".equalsIgnoreCase(provider)) {
            return generateImageWithAliyun(prompt);
        } else {
            throw new BusinessException(ResultCode.ERROR, "不支持的AI服务商：" + provider + "，请使用 aliyun");
        }
    }
    
    /**
     * 使用阿里云DashScope生成文本（通义千问）
     */
    private TextGenerateResponse generateTextWithAliyun(String prompt, String style) {
        try {
            AiConfig.Aliyun config = aiConfig.getAliyun();
            if (config == null) {
                throw new BusinessException(ResultCode.ERROR, "阿里云配置未找到，请检查application.yml");
            }
            
            String apiKey = config.getApiKey();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("API Key为空，请检查配置");
                throw new BusinessException(ResultCode.ERROR, "API Key未配置，请在application.yml中配置ai.aliyun.api-key");
            }
            
            // 只显示API Key的前4位和后4位，用于调试
            String maskedKey = apiKey.length() > 8 
                ? apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4)
                : "***";
            log.info("使用API Key: {}", maskedKey);
            
            // 使用兼容OpenAI模式的接口（更稳定）
            String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            // 兼容模式也可以使用 X-DashScope-API-Key，但Authorization更通用
            headers.set("X-DashScope-API-Key", apiKey);
            
            // 构建提示词（如果指定了风格，添加到提示词中）
            String fullPrompt = prompt;
            if (style != null && !style.isEmpty()) {
                fullPrompt = String.format("请按照%s风格生成内容：%s", style, prompt);
            }
            
            // 使用OpenAI兼容格式
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel() != null ? config.getModel() : "qwen-plus");
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "user", "content", fullPrompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            log.info("文本生成请求URL: {}, 模型: {}", url, config.getModel());
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            log.info("文本生成响应状态码: {}, 响应体: {}", response.getStatusCode(), response.getBody());
            
            // 检查HTTP状态码
            if (!response.getStatusCode().is2xxSuccessful()) {
                String errorMsg = "HTTP请求失败，状态码: " + response.getStatusCode();
                if (response.getStatusCode().value() == 401) {
                    throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置。响应: " + response.getBody());
                }
                throw new BusinessException(ResultCode.ERROR, errorMsg + "，响应: " + response.getBody());
            }
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            // 处理错误响应（兼容模式和原生API的错误格式可能不同）
            if (jsonNode.has("error")) {
                JsonNode error = jsonNode.get("error");
                String errorType = error.has("type") ? error.get("type").asText() : "";
                String errorMsg = error.has("message") ? error.get("message").asText() : "文本生成失败";
                
                log.error("阿里云API返回错误，type: {}, message: {}", errorType, errorMsg);
                
                if ("invalid_api_key".equals(errorType) || "invalid_request_error".equals(errorType) 
                    || errorMsg.contains("Invalid API Key") || errorMsg.contains("invalid_api_key")) {
                    throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置。错误信息: " + errorMsg);
                } else if (errorMsg.contains("Insufficient Balance") || errorMsg.contains("余额不足")) {
                    throw new BusinessException(ResultCode.ERROR, "账户余额不足，请充值");
                }
                
                throw new BusinessException(ResultCode.ERROR, "文本生成失败：" + errorMsg);
            }
            
            if (jsonNode.has("code") && !"Success".equals(jsonNode.get("code").asText())) {
                String errorCode = jsonNode.get("code").asText();
                String errorMsg = jsonNode.has("message") 
                    ? jsonNode.get("message").asText()
                    : "文本生成失败";
                
                log.error("阿里云API返回错误，code: {}, message: {}", errorCode, errorMsg);
                
                if ("InvalidApiKey".equals(errorCode) || "Unauthorized".equals(errorCode)) {
                    throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置。错误信息: " + errorMsg);
                } else if ("InsufficientBalance".equals(errorCode)) {
                    throw new BusinessException(ResultCode.ERROR, "账户余额不足，请充值");
                }
                
                throw new BusinessException(ResultCode.ERROR, "文本生成失败：" + errorMsg);
            }
            
            // 解析响应数据（兼容OpenAI格式）
            String content = "";
            if (jsonNode.has("choices") && jsonNode.get("choices").isArray() && jsonNode.get("choices").size() > 0) {
                JsonNode firstChoice = jsonNode.get("choices").get(0);
                if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                    content = firstChoice.get("message").get("content").asText();
                }
            } else if (jsonNode.has("output")) {
                // 原生API格式（备用）
                JsonNode output = jsonNode.get("output");
                if (output.has("text")) {
                    content = output.get("text").asText();
                } else if (output.has("choices") && output.get("choices").isArray() && output.get("choices").size() > 0) {
                    JsonNode firstChoice = output.get("choices").get(0);
                    if (firstChoice.has("message")) {
                        content = firstChoice.get("message").get("content").asText();
                    }
                }
            }
            
            if (content.isEmpty()) {
                log.error("未获取到生成内容，响应: {}", response.getBody());
                throw new BusinessException(ResultCode.ERROR, "文本生成失败：未获取到生成内容");
            }
            
            return new TextGenerateResponse(content, config.getModel() != null ? config.getModel() : "qwen-plus");
        } catch (BusinessException e) {
            throw e;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP客户端错误，状态码: {}, 响应体: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode() != null && e.getStatusCode().value() == 401) {
                throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置。响应: " + e.getResponseBodyAsString());
            }
            throw new BusinessException(ResultCode.ERROR, "请求失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("阿里云文本生成失败：", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("401")) {
                throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置");
            } else if (errorMsg != null && errorMsg.contains("429")) {
                throw new BusinessException(ResultCode.ERROR, "请求过于频繁，请稍后再试");
            } else if (errorMsg != null && (errorMsg.contains("Insufficient Balance") || errorMsg.contains("402"))) {
                throw new BusinessException(ResultCode.ERROR, "账户余额不足，请充值");
            }
            throw new BusinessException(ResultCode.ERROR, "文本生成失败：" + errorMsg);
        }
    }
    
    /**
     * 使用百度文心一言生成文本（已废弃，保留用于兼容）
     * @deprecated 已切换到阿里云DashScope
     */
    @Deprecated
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
     * 使用阿里云DashScope生成图片（通义万相）
     */
    private ImageGenerateResponse generateImageWithAliyun(String prompt) {
        return generateImageWithAliyun(prompt, "1024*1024");
    }
    
    private ImageGenerateResponse generateImageWithAliyun(String prompt, String size) {
        try {
            AiConfig.Aliyun config = aiConfig.getAliyun();
            if (config == null) {
                throw new BusinessException(ResultCode.ERROR, "阿里云配置未找到，请检查application.yml");
            }
            
            String apiKey = config.getApiKey();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("API Key为空，请检查配置");
                throw new BusinessException(ResultCode.ERROR, "API Key未配置，请在application.yml中配置ai.aliyun.apiKey");
            }
            
            // 只显示API Key的前4位和后4位，用于调试
            String maskedKey = apiKey.length() > 8 
                ? apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4)
                : "***";
            log.info("图片生成使用API Key: {}", maskedKey);
            
            // 使用正确的图片生成API端点（根据官方文档）
            String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 官方文档要求：只需要 Authorization Bearer，不需要 X-DashScope-API-Key
            headers.set("Authorization", "Bearer " + apiKey);
            // 必须使用异步模式（官方文档要求）
            headers.set("X-DashScope-Async", "enable");
            
            Map<String, Object> requestBody = new HashMap<>();
            // 对于纯文生图，使用 wan2.6-t2i 模型（官方推荐）
            // wan2.6-image 是用于图文混排的，不是纯文生图
            String imageModel = config.getImageModel();
            if (imageModel == null || "wanx-v1".equals(imageModel)) {
                imageModel = "wan2.6-t2i"; // 使用官方推荐的文生图模型
            }
            requestBody.put("model", imageModel);
            
            // 使用messages格式（符合DashScope API规范）
            Map<String, Object> input = new HashMap<>();
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            
            // 内容格式：包含text字段
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("text", prompt);
            content.add(textContent);
            userMessage.put("content", content);
            
            messages.add(userMessage);
            input.put("messages", messages);
            requestBody.put("input", input);
            
            Map<String, Object> parameters = new HashMap<>();
            // 通义万相支持的尺寸格式：1024*1024 (正方形), 720*1280 (竖图), 1280*720 (横图)
            // 注意：使用*号而不是x号
            parameters.put("size", "1024*1024");
            parameters.put("n", 1);
            // 官方文档推荐的参数
            parameters.put("prompt_extend", true); // 智能扩展提示词
            parameters.put("watermark", false); // 不加水印
            parameters.put("negative_prompt", ""); // 负面提示词（可选）
            requestBody.put("parameters", parameters);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            log.info("图片生成请求URL: {}, 模型: {}, 请求体: {}", url, imageModel, objectMapper.writeValueAsString(requestBody));
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            log.info("图片生成响应状态码: {}, 响应体: {}", response.getStatusCode(), response.getBody());
            
            // 检查HTTP状态码
            if (!response.getStatusCode().is2xxSuccessful()) {
                String errorMsg = "HTTP请求失败，状态码: " + response.getStatusCode();
                if (response.getStatusCode().value() == 401) {
                    throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置。响应: " + response.getBody());
                }
                throw new BusinessException(ResultCode.ERROR, errorMsg + "，响应: " + response.getBody());
            }
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            List<String> imageUrls = new ArrayList<>();
            
            // 检查是否是异步任务（返回task_id）
            if (jsonNode.has("output") && jsonNode.get("output").has("task_id")) {
                String taskId = jsonNode.get("output").get("task_id").asText();
                log.info("图片生成任务已提交，task_id: {}", taskId);
                
                // 轮询查询任务状态，直到完成
                String imageUrl = pollImageGenerationTask(taskId, apiKey);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    imageUrls.add(imageUrl);
                } else {
                    throw new BusinessException(ResultCode.ERROR, "图片生成任务失败或超时");
                }
            } else {
                // 同步响应，直接解析结果
                // 处理错误响应（兼容多种错误格式）
                if (jsonNode.has("error")) {
                    JsonNode error = jsonNode.get("error");
                    String errorType = error.has("type") ? error.get("type").asText() : "";
                    String errorMsg = error.has("message") ? error.get("message").asText() : "图片生成失败";
                    
                    log.error("阿里云API返回错误，type: {}, message: {}", errorType, errorMsg);
                    
                    if ("invalid_api_key".equals(errorType) || "invalid_request_error".equals(errorType) 
                        || errorMsg.contains("Invalid API Key") || errorMsg.contains("invalid_api_key")) {
                        throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置。错误信息: " + errorMsg);
                    } else if (errorMsg.contains("Insufficient Balance") || errorMsg.contains("余额不足")) {
                        throw new BusinessException(ResultCode.ERROR, "账户余额不足，请充值");
                    }
                    
                    throw new BusinessException(ResultCode.ERROR, "图片生成失败：" + errorMsg);
                }
                
                if (jsonNode.has("code") && !"Success".equals(jsonNode.get("code").asText())) {
                    String errorCode = jsonNode.get("code").asText();
                    String errorMsg = jsonNode.has("message") 
                        ? jsonNode.get("message").asText()
                        : "图片生成失败";
                    
                    log.error("阿里云API返回错误，code: {}, message: {}", errorCode, errorMsg);
                    
                    if ("InvalidApiKey".equals(errorCode) || "Unauthorized".equals(errorCode)) {
                        throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置。错误信息: " + errorMsg);
                    } else if ("InsufficientBalance".equals(errorCode)) {
                        throw new BusinessException(ResultCode.ERROR, "账户余额不足，请充值");
                    }
                    
                    throw new BusinessException(ResultCode.ERROR, "图片生成失败：" + errorMsg);
                }
                
                // 解析响应数据
                if (jsonNode.has("output")) {
                    JsonNode output = jsonNode.get("output");
                    if (output.has("results")) {
                        output.get("results").forEach(result -> {
                            if (result.has("url")) {
                                String imageUrl = result.get("url").asText();
                                log.info("获取到图片URL: {}", imageUrl);
                                imageUrls.add(imageUrl);
                            } else if (result.has("b64_image")) {
                                // Base64图片
                                String b64Image = result.get("b64_image").asText();
                                log.info("获取到Base64图片，长度: {}", b64Image.length());
                                imageUrls.add("data:image/png;base64," + b64Image);
                            } else {
                                log.warn("图片结果中未找到url或b64_image字段: {}", result.toString());
                            }
                        });
                    } else {
                        log.warn("output中未找到results字段，完整响应: {}", response.getBody());
                    }
                } else {
                    log.error("响应中未找到output字段，完整响应: {}", response.getBody());
                }
                
                if (imageUrls.isEmpty()) {
                    log.error("未获取到图片数据，完整响应: {}", response.getBody());
                    throw new BusinessException(ResultCode.ERROR, "图片生成失败：未获取到图片数据。请查看日志了解详情。");
                }
            }
            
            return new ImageGenerateResponse(imageUrls, config.getImageModel() != null ? config.getImageModel() : "wan2.6-t2i");
        } catch (BusinessException e) {
            throw e;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP客户端错误，状态码: {}, 响应体: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode() != null && e.getStatusCode().value() == 401) {
                throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置。响应: " + e.getResponseBodyAsString());
            }
            throw new BusinessException(ResultCode.ERROR, "请求失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("阿里云图片生成失败：", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("401")) {
                throw new BusinessException(ResultCode.ERROR, "API Key无效，请检查配置");
            } else if (errorMsg != null && errorMsg.contains("429")) {
                throw new BusinessException(ResultCode.ERROR, "请求过于频繁，请稍后再试");
            } else if (errorMsg != null && errorMsg.contains("500")) {
                throw new BusinessException(ResultCode.ERROR, "服务器内部错误，请稍后再试");
            } else if (errorMsg != null && (errorMsg.contains("Insufficient Balance") || errorMsg.contains("402"))) {
                throw new BusinessException(ResultCode.ERROR, "账户余额不足，请充值");
            }
            throw new BusinessException(ResultCode.ERROR, "图片生成失败：" + errorMsg);
        }
    }
    
    /**
     * 轮询查询图片生成任务状态
     * @param taskId 任务ID
     * @param apiKey API密钥
     * @return 图片URL，如果任务失败或超时返回null
     */
    private String pollImageGenerationTask(String taskId, String apiKey) {
        String url = "https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId;
        
        HttpHeaders headers = new HttpHeaders();
        // 官方文档要求：只需要 Authorization Bearer
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        int maxAttempts = 30; // 最多轮询30次
        int interval = 2000; // 每次间隔2秒
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            try {
                Thread.sleep(interval);
                attempt++;
                
                log.info("查询图片生成任务状态，task_id: {}, 第{}次查询", taskId, attempt);
                
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
                
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.error("查询任务状态失败，状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                    continue;
                }
                
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                // 打印完整响应以便调试
                log.debug("任务查询响应: {}", response.getBody());
                
                // 检查任务状态
                if (jsonNode.has("output")) {
                    JsonNode output = jsonNode.get("output");
                    String taskStatus = output.has("task_status") ? output.get("task_status").asText() : "";
                    
                    log.info("任务状态: {}", taskStatus);
                    
                    if ("SUCCEEDED".equals(taskStatus) || "SUCCESS".equals(taskStatus)) {
                        // 任务成功，获取图片URL
                        log.info("任务成功，开始解析结果，output结构: {}", output.toString());
                        
                        // 尝试多种可能的响应结构
                        String imageUrl = null;
                        
                        // 方式1: output.results[0].url
                        if (output.has("results") && output.get("results").isArray() && output.get("results").size() > 0) {
                            JsonNode firstResult = output.get("results").get(0);
                            log.info("找到results数组，第一个结果: {}", firstResult.toString());
                            
                            if (firstResult.has("url")) {
                                imageUrl = firstResult.get("url").asText();
                                log.info("从results[0].url获取到图片URL: {}", imageUrl);
                            } else if (firstResult.has("b64_image")) {
                                String b64Image = firstResult.get("b64_image").asText();
                                log.info("从results[0].b64_image获取到Base64图片，长度: {}", b64Image.length());
                                imageUrl = "data:image/png;base64," + b64Image;
                            } else if (firstResult.has("image_url")) {
                                imageUrl = firstResult.get("image_url").asText();
                                log.info("从results[0].image_url获取到图片URL: {}", imageUrl);
                            }
                        }
                        
                        // 方式2: output.url (直接URL)
                        if (imageUrl == null && output.has("url")) {
                            imageUrl = output.get("url").asText();
                            log.info("从output.url获取到图片URL: {}", imageUrl);
                        }
                        
                        // 方式3: output.images[0].url
                        if (imageUrl == null && output.has("images") && output.get("images").isArray() && output.get("images").size() > 0) {
                            JsonNode firstImage = output.get("images").get(0);
                            if (firstImage.has("url")) {
                                imageUrl = firstImage.get("url").asText();
                                log.info("从output.images[0].url获取到图片URL: {}", imageUrl);
                            }
                        }
                        
                        // 方式4: output.choices[0].message.content[0].image (官方实际返回格式)
                        if (imageUrl == null && output.has("choices") && output.get("choices").isArray() && output.get("choices").size() > 0) {
                            JsonNode firstChoice = output.get("choices").get(0);
                            log.info("找到choices数组，第一个choice: {}", firstChoice.toString());
                            
                            if (firstChoice.has("message")) {
                                JsonNode message = firstChoice.get("message");
                                if (message.has("content") && message.get("content").isArray() && message.get("content").size() > 0) {
                                    JsonNode firstContent = message.get("content").get(0);
                                    log.info("找到content数组，第一个content: {}", firstContent.toString());
                                    
                                    if (firstContent.has("image")) {
                                        imageUrl = firstContent.get("image").asText();
                                        log.info("从output.choices[0].message.content[0].image获取到图片URL: {}", imageUrl);
                                    } else if (firstContent.has("url")) {
                                        imageUrl = firstContent.get("url").asText();
                                        log.info("从output.choices[0].message.content[0].url获取到图片URL: {}", imageUrl);
                                    }
                                }
                            }
                        }
                        
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            log.info("图片生成成功，最终URL: {}", imageUrl);
                            return imageUrl;
                        } else {
                            log.error("任务成功但未找到图片URL，完整output: {}", output.toString());
                            // 即使没找到URL，也不继续轮询，直接抛出异常
                            throw new BusinessException(ResultCode.ERROR, "任务成功但未找到图片URL，请查看日志了解详情");
                        }
                    } else if ("FAILED".equals(taskStatus) || "FAILURE".equals(taskStatus)) {
                        String errorMsg = output.has("message") ? output.get("message").asText() : "任务失败";
                        log.error("图片生成任务失败: {}", errorMsg);
                        throw new BusinessException(ResultCode.ERROR, "图片生成任务失败：" + errorMsg);
                    } else if ("PENDING".equals(taskStatus) || "RUNNING".equals(taskStatus) || "PROCESSING".equals(taskStatus)) {
                        // 任务进行中，继续轮询
                        continue;
                    }
                }
                
                // 检查错误响应
                if (jsonNode.has("code") && !"Success".equals(jsonNode.get("code").asText())) {
                    String errorCode = jsonNode.get("code").asText();
                    String errorMsg = jsonNode.has("message") ? jsonNode.get("message").asText() : "查询任务失败";
                    log.error("查询任务失败，code: {}, message: {}", errorCode, errorMsg);
                    throw new BusinessException(ResultCode.ERROR, "查询任务失败：" + errorMsg);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("轮询任务被中断", e);
                throw new BusinessException(ResultCode.ERROR, "查询任务被中断");
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("查询任务状态异常，继续重试", e);
                // 继续重试
            }
        }
        
        log.error("图片生成任务超时，task_id: {}, 已轮询{}次", taskId, attempt);
        throw new BusinessException(ResultCode.ERROR, "图片生成任务超时，请稍后重试");
    }
    
    /**
     * 使用百度生成图片（已废弃，保留用于兼容）
     * @deprecated 已切换到阿里云DashScope
     */
    @Deprecated
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
     * @deprecated 已切换到阿里云DashScope
     */
    @Deprecated
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
