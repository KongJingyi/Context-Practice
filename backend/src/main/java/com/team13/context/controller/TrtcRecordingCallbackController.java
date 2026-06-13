package com.team13.context.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team13.context.config.TrtcProperties;
import com.team13.context.trtc.TrtcCloudRecordingService;
import com.team13.context.trtc.TrtcRecordingCallbackSignVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/trtc")
@RequiredArgsConstructor
public class TrtcRecordingCallbackController {

    private final TrtcCloudRecordingService trtcCloudRecordingService;
    private final TrtcProperties trtcProperties;
    private final ObjectMapper objectMapper;

    /**
     * 腾讯云 TRTC 录制回调（EventType=311）或内部测试格式。
     * 控制台配置：应用 → 回调配置 → 录制回调 → 指向此 URL。
     */
    @PostMapping("/recording-callback")
    public ResponseEntity<Map<String, Object>> recordingCallback(
            @RequestBody String rawBody,
            @RequestHeader(value = "Sign", required = false) String sign,
            @RequestHeader(value = "SdkAppId", required = false) String sdkAppId) {
        if (!TrtcRecordingCallbackSignVerifier.verify(
                trtcProperties.getRecordingCallbackSecret(), rawBody, sign)) {
            log.warn("Recording callback sign verification failed sdkAppId={}", sdkAppId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", -1, "message", "invalid callback sign"));
        }
        try {
            Map<String, Object> body = objectMapper.readValue(rawBody, new TypeReference<>() {});
            trtcCloudRecordingService.handleCallback(body);
        } catch (Exception ex) {
            log.warn("Recording callback parse/handle failed: {}", ex.getMessage());
            return ResponseEntity.ok(Map.of("code", -1, "message", "callback handle failed"));
        }
        return ResponseEntity.ok(Map.of("code", 0));
    }
}
