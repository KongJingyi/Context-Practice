package com.team13.context.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team13.context.common.UserContext;
import com.team13.context.entity.AuditLog;
import com.team13.context.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public void record(String action, String targetType, Long targetId, String detail) {
        Long actorId = UserContext.getUserId();
        if (actorId == null) {
            return;
        }
        AuditLog log = new AuditLog();
        log.setActorId(actorId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(toJsonDetail(detail));
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }

    /** ctx_audit_log.detail 为 JSON 列，须写入合法 JSON。 */
    private String toJsonDetail(String detail) {
        if (!StringUtils.hasText(detail)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(Map.of("message", detail));
        } catch (JsonProcessingException e) {
            return "{\"message\":\"audit\"}";
        }
    }
}
