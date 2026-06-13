package com.team13.context.room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team13.context.room.RoomPressureState.Countdown;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomLiveStore {

    private static final String STATE_PREFIX = "room:state:";
    private static final String CHAT_PREFIX = "room:chat:";
    private static final String WHITEBOARD_PREFIX = "room:whiteboard:";
    private static final int MAX_CHAT = 200;
    private static final int MAX_WHITEBOARD_STROKES = 800;
    private static final Duration STATE_TTL = Duration.ofHours(24);

    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private final ConcurrentHashMap<String, String> memoryState = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> memoryWhiteboard = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> memoryChat =
            new ConcurrentHashMap<>();

    public RoomPressureState getPressureState(String roomId) {
        String json = readStateJson(roomId);
        if (json == null || json.isBlank()) {
            return emptyPressureState();
        }
        try {
            return objectMapper.readValue(json, RoomPressureState.class);
        } catch (JsonProcessingException e) {
            log.warn("parse room pressure state failed: {}", roomId, e);
            return emptyPressureState();
        }
    }

    public void savePressureState(String roomId, RoomPressureState state) {
        try {
            writeStateJson(roomId, objectMapper.writeValueAsString(state));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("保存房间状态失败", e);
        }
    }

    public RoomChatMessage appendChat(String roomId, RoomChatMessage message) {
        if (message.getId() == null || message.getId().isBlank()) {
            message.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        try {
            String json = objectMapper.writeValueAsString(message);
            if (stringRedisTemplate != null) {
                String key = CHAT_PREFIX + roomId;
                stringRedisTemplate.opsForList().rightPush(key, json);
                Long size = stringRedisTemplate.opsForList().size(key);
                if (size != null && size > MAX_CHAT) {
                    stringRedisTemplate.opsForList().trim(key, size - MAX_CHAT, -1);
                }
                stringRedisTemplate.expire(key, STATE_TTL);
            } else {
                memoryChat
                        .computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>())
                        .add(json);
                trimMemoryChat(roomId);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("保存聊天消息失败", e);
        }
        return message;
    }

    public List<RoomChatMessage> listChat(String roomId, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 50;
        }
        List<String> raw = readChatJsonList(roomId);
        if (raw.isEmpty()) {
            return List.of();
        }
        int total = raw.size();
        int from = Math.max(0, total - page * size);
        int to = Math.max(0, total - (page - 1) * size);
        if (from >= to) {
            return List.of();
        }
        List<RoomChatMessage> result = new ArrayList<>();
        for (int i = from; i < to; i++) {
            try {
                result.add(objectMapper.readValue(raw.get(i), RoomChatMessage.class));
            } catch (JsonProcessingException e) {
                log.warn("skip bad chat json at index {}", i);
            }
        }
        return result;
    }

    public RoomWhiteboardState getWhiteboardState(String roomId) {
        String json = readWhiteboardJson(roomId);
        if (json == null || json.isBlank()) {
            return emptyWhiteboardState();
        }
        try {
            return objectMapper.readValue(json, RoomWhiteboardState.class);
        } catch (JsonProcessingException e) {
            log.warn("parse room whiteboard state failed: {}", roomId, e);
            return emptyWhiteboardState();
        }
    }

    public void saveWhiteboardState(String roomId, RoomWhiteboardState state) {
        try {
            writeWhiteboardJson(roomId, objectMapper.writeValueAsString(state));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("保存白板状态失败", e);
        }
    }

    public RoomWhiteboardState appendWhiteboardStrokes(String roomId, List<RoomWhiteboardStroke> strokes) {
        RoomWhiteboardState state = getWhiteboardState(roomId);
        if (strokes == null || strokes.isEmpty()) {
            return state;
        }
        state.getStrokes().addAll(strokes);
        trimWhiteboardStrokes(state);
        state.setVersion(state.getVersion() + 1);
        saveWhiteboardState(roomId, state);
        return state;
    }

    public RoomWhiteboardState clearWhiteboard(String roomId) {
        RoomWhiteboardState state = getWhiteboardState(roomId);
        state.getStrokes().clear();
        state.setVersion(state.getVersion() + 1);
        saveWhiteboardState(roomId, state);
        return state;
    }

    public void clearRoom(String roomId) {
        if (stringRedisTemplate != null) {
            stringRedisTemplate.delete(STATE_PREFIX + roomId);
            stringRedisTemplate.delete(CHAT_PREFIX + roomId);
            stringRedisTemplate.delete(WHITEBOARD_PREFIX + roomId);
        }
        memoryState.remove(roomId);
        memoryWhiteboard.remove(roomId);
        memoryChat.remove(roomId);
    }

    public static RoomPressureState emptyPressureState() {
        RoomPressureState state = new RoomPressureState();
        state.setEnabled(false);
        state.setCountdown(new Countdown());
        return state;
    }

    public static RoomWhiteboardState emptyWhiteboardState() {
        RoomWhiteboardState state = new RoomWhiteboardState();
        state.setActive(false);
        state.setVersion(0);
        return state;
    }

    private void trimWhiteboardStrokes(RoomWhiteboardState state) {
        while (state.getStrokes().size() > MAX_WHITEBOARD_STROKES) {
            state.getStrokes().remove(0);
        }
    }

    private String readWhiteboardJson(String roomId) {
        if (stringRedisTemplate != null) {
            return stringRedisTemplate.opsForValue().get(WHITEBOARD_PREFIX + roomId);
        }
        return memoryWhiteboard.get(roomId);
    }

    private void writeWhiteboardJson(String roomId, String json) {
        if (stringRedisTemplate != null) {
            stringRedisTemplate.opsForValue().set(WHITEBOARD_PREFIX + roomId, json, STATE_TTL);
            return;
        }
        memoryWhiteboard.put(roomId, json);
    }

    private String readStateJson(String roomId) {
        if (stringRedisTemplate != null) {
            return stringRedisTemplate.opsForValue().get(STATE_PREFIX + roomId);
        }
        return memoryState.get(roomId);
    }

    private void writeStateJson(String roomId, String json) {
        if (stringRedisTemplate != null) {
            stringRedisTemplate.opsForValue().set(STATE_PREFIX + roomId, json, STATE_TTL);
            return;
        }
        memoryState.put(roomId, json);
    }

    private List<String> readChatJsonList(String roomId) {
        if (stringRedisTemplate != null) {
            List<String> list = stringRedisTemplate.opsForList().range(CHAT_PREFIX + roomId, 0, -1);
            return list != null ? list : List.of();
        }
        CopyOnWriteArrayList<String> list = memoryChat.get(roomId);
        return list == null ? List.of() : new ArrayList<>(list);
    }

    private void trimMemoryChat(String roomId) {
        CopyOnWriteArrayList<String> list = memoryChat.get(roomId);
        if (list == null) {
            return;
        }
        while (list.size() > MAX_CHAT) {
            list.remove(0);
        }
    }
}
