package com.team13.context.service.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PostMetaSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Map<String, Object> parseMeta(String mediaUrls) {
        if (mediaUrls == null || mediaUrls.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return MAPPER.readValue(mediaUrls, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> tagsFromMeta(Map<String, Object> meta) {
        Object tags = meta.get("tags");
        if (tags instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public String kindToType(String kind, Map<String, Object> meta) {
        Object type = meta.get("type");
        if (type != null) {
            return String.valueOf(type);
        }
        return switch (kind != null ? kind : "") {
            case "HIGHLIGHT" -> "highlight";
            case "EXPERIENCE" -> "interview";
            default -> "insight";
        };
    }
}
