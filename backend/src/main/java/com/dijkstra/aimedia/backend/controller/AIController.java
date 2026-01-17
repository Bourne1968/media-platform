package com.dijkstra.aimedia.backend.controller;

import com.dijkstra.aimedia.backend.common.Result;
import com.dijkstra.aimedia.backend.dto.ImageGenerateRequest;
import com.dijkstra.aimedia.backend.dto.ImageGenerateResponse;
import com.dijkstra.aimedia.backend.dto.TextGenerateRequest;
import com.dijkstra.aimedia.backend.dto.TextGenerateResponse;
import com.dijkstra.aimedia.backend.service.AIService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI生成控制器
 * 
 * @author dijkstra
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {
    
    private final AIService aiService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    /**
     * 生成文本内容（文案或标题）
     * 
     * @param request 文本生成请求
     * @param httpRequest HTTP请求
     * @return 生成的文本
     */
    @PostMapping("/text")
    public Result<TextGenerateResponse> generateText(
            @Valid @RequestBody TextGenerateRequest request,
            HttpServletRequest httpRequest) {
        TextGenerateResponse response = aiService.generateText(request.getPrompt(), request.getStyle());
        return Result.success("生成成功", response);
    }
    
    /**
     * 生成图片
     * 
     * @param request 图片生成请求
     * @param httpRequest HTTP请求
     * @return 生成的图片URL
     */
    @PostMapping("/image")
    public Result<ImageGenerateResponse> generateImage(
            @Valid @RequestBody ImageGenerateRequest request,
            HttpServletRequest httpRequest) {
        String size = request.getSize() != null ? request.getSize() : "1024*1024";
        ImageGenerateResponse response = aiService.generateImage(request.getPrompt(), size);
        return Result.success("生成成功", response);
    }
    
    /**
     * 流式生成文本（SSE）
     * 前端可以使用EventSource接收流式数据
     * 
     * @param prompt 提示词
     * @param style 风格模板
     * @param httpRequest HTTP请求
     * @return SSE流
     */
    @GetMapping(value = "/text/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateTextStream(
            @RequestParam String prompt,
            @RequestParam(required = false) String style,
            HttpServletRequest httpRequest) {
        
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时
        
        CompletableFuture.runAsync(() -> {
            try {
                // 生成完整文本
                TextGenerateResponse response = aiService.generateText(prompt, style);
                String content = response.getContent();
                
                // 模拟流式输出，逐字符发送
                // 注意：实际AI API如果支持流式输出，应该直接使用API的流式接口
                // 这里为了演示，将完整文本分段发送
                char[] chars = content.toCharArray();
                StringBuilder currentChunk = new StringBuilder();
                
                for (int i = 0; i < chars.length; i++) {
                    currentChunk.append(chars[i]);
                    
                    // 每5个字符或遇到标点符号时发送一次
                    if (currentChunk.length() >= 5 || 
                        (i > 0 && (chars[i] == '。' || chars[i] == '！' || chars[i] == '？' || 
                                  chars[i] == '.' || chars[i] == '!' || chars[i] == '?' || chars[i] == '\n'))) {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(currentChunk.toString()));
                        currentChunk.setLength(0);
                        
                        // 模拟网络延迟，让前端看到逐字显示效果
                        Thread.sleep(50);
                    }
                }
                
                // 发送剩余内容
                if (currentChunk.length() > 0) {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(currentChunk.toString()));
                }
                
                // 发送结束标记
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("true"));
                
                emitter.complete();
            } catch (Exception e) {
                log.error("流式生成文本失败：", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("生成失败：" + e.getMessage()));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        }, executorService);
        
        // 处理客户端断开连接
        emitter.onCompletion(() -> log.info("SSE连接已关闭"));
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });
        emitter.onError((ex) -> {
            log.error("SSE连接出错：", ex);
            emitter.completeWithError(ex);
        });
        
        return emitter;
    }
    
    /**
     * 高级流式生成文本（支持实际AI API流式响应）
     * 注意：此方法需要AI API本身支持流式输出（如OpenAI的stream模式）
     * 当前实现是模拟流式输出，实际使用时需要根据具体API调整
     * 
     * @param request 文本生成请求
     * @param httpRequest HTTP请求
     * @return SSE流
     */
    @PostMapping(value = "/text/stream-advanced", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateTextStreamAdvanced(
            @Valid @RequestBody TextGenerateRequest request,
            HttpServletRequest httpRequest) {
        
        SseEmitter emitter = new SseEmitter(120000L); // 120秒超时（生成可能较慢）
        
        CompletableFuture.runAsync(() -> {
            try {
                // 发送开始事件
                emitter.send(SseEmitter.event()
                        .name("start")
                        .data("开始生成..."));
                
                // 调用AI服务生成文本
                TextGenerateResponse response = aiService.generateText(request.getPrompt(), request.getStyle());
                String content = response.getContent();
                
                // 模拟实时生成效果（实际应该使用API的流式接口）
                String[] sentences = content.split("[。！？\n]");
                
                for (String sentence : sentences) {
                    if (sentence.trim().isEmpty()) {
                        continue;
                    }
                    
                    // 逐字发送句子
                    for (char c : sentence.toCharArray()) {
                        emitter.send(SseEmitter.event()
                                .name("chunk")
                                .data(String.valueOf(c)));
                        Thread.sleep(30); // 控制发送速度
                    }
                    
                    // 发送句子结束标记
                    emitter.send(SseEmitter.event()
                            .name("chunk")
                            .data("。"));
                    
                    Thread.sleep(100);
                }
                
                // 发送完成事件
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("生成完成"));
                
                emitter.complete();
            } catch (Exception e) {
                log.error("流式生成文本失败：", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("生成失败：" + e.getMessage()));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        }, executorService);
        
        emitter.onCompletion(() -> log.info("SSE连接已关闭"));
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });
        emitter.onError((ex) -> {
            log.error("SSE连接出错：", ex);
            emitter.completeWithError(ex);
        });
        
        return emitter;
    }
}
