package com.team13.context.service.impl;

import com.team13.context.common.UserContext;
import com.team13.context.dto.ChatMessagePageResponse;
import com.team13.context.dto.ChatMessageResponse;
import com.team13.context.dto.PressureCountdownRequest;
import com.team13.context.dto.PressureCountdownResponse;
import com.team13.context.dto.PressureInterruptRequest;
import com.team13.context.dto.PressureQuestionRequest;
import com.team13.context.dto.RoomCountdownView;
import com.team13.context.dto.RoomInterruptView;
import com.team13.context.dto.RoomLiveStateResponse;
import com.team13.context.dto.RoomPressureModeView;
import com.team13.context.dto.RoomQuestionView;
import com.team13.context.dto.RoomWhiteboardStrokeView;
import com.team13.context.dto.RoomWhiteboardView;
import com.team13.context.dto.WhiteboardStrokeBatchRequest;
import com.team13.context.dto.WhiteboardStrokeInput;
import com.team13.context.dto.WhiteboardToggleRequest;
import com.team13.context.room.RoomChatMessage;
import com.team13.context.room.RoomLiveStore;
import com.team13.context.room.RoomPressureState;
import com.team13.context.room.RoomPressureState.Countdown;
import com.team13.context.room.RoomPressureState.Interrupt;
import com.team13.context.room.RoomPressureState.Question;
import com.team13.context.room.RoomWhiteboardState;
import com.team13.context.room.RoomWhiteboardStroke;
import com.team13.context.service.RoomInteractionService;
import com.team13.context.service.support.RoomContext;
import com.team13.context.service.support.RoomContextSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomInteractionServiceImpl implements RoomInteractionService {

    private final RoomContextSupport roomContextSupport;
    private final RoomLiveStore roomLiveStore;

    @Override
    public RoomLiveStateResponse getLiveState(String roomId) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        return buildLiveStateResponse(roomId);
    }

    @Override
    public PressureCountdownResponse controlCountdown(String roomId, PressureCountdownRequest request) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        roomContextSupport.assertCoach(ctx);
        roomContextSupport.assertTrainingInProgress(ctx);

        RoomPressureState state = roomLiveStore.getPressureState(roomId);
        Countdown countdown = state.getCountdown() != null ? state.getCountdown() : new Countdown();
        String action = request.getAction().trim().toLowerCase();

        switch (action) {
            case "start" -> {
                int seconds = request.getSeconds() != null && request.getSeconds() > 0 ? request.getSeconds() : 60;
                countdown.setActive(true);
                countdown.setTotalSeconds(seconds);
                countdown.setStartedAtEpochMs(System.currentTimeMillis());
                state.setEnabled(true);
            }
            case "stop" -> {
                countdown.setActive(false);
                countdown.setStartedAtEpochMs(null);
            }
            case "reset" -> {
                if (countdown.getTotalSeconds() != null && countdown.getTotalSeconds() > 0) {
                    countdown.setActive(true);
                    countdown.setStartedAtEpochMs(System.currentTimeMillis());
                    state.setEnabled(true);
                } else {
                    countdown.setActive(false);
                }
            }
            default -> throw new IllegalArgumentException("不支持的 action: " + request.getAction());
        }
        state.setCountdown(countdown);
        roomLiveStore.savePressureState(roomId, state);

        RoomCountdownView view = resolveCountdownView(countdown);
        return PressureCountdownResponse.builder()
                .active(view.getActive())
                .secondsLeft(view.getSecondsLeft())
                .totalSeconds(view.getTotalSeconds())
                .build();
    }

    @Override
    public RoomLiveStateResponse interrupt(String roomId, PressureInterruptRequest request) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        roomContextSupport.assertCoach(ctx);
        roomContextSupport.assertTrainingInProgress(ctx);

        RoomPressureState state = roomLiveStore.getPressureState(roomId);
        Interrupt interrupt = new Interrupt();
        interrupt.setMessage(request.getMessage().trim());
        interrupt.setAt(LocalDateTime.now());
        state.setLastInterrupt(interrupt);
        state.setEnabled(true);
        roomLiveStore.savePressureState(roomId, state);
        return buildLiveStateResponse(roomId);
    }

    @Override
    public RoomLiveStateResponse askQuestion(String roomId, PressureQuestionRequest request) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        roomContextSupport.assertCoach(ctx);
        roomContextSupport.assertTrainingInProgress(ctx);

        RoomPressureState state = roomLiveStore.getPressureState(roomId);
        Question question = new Question();
        question.setQuestionId(request.getQuestionId());
        question.setText(request.getText().trim());
        question.setAt(LocalDateTime.now());
        state.setCurrentQuestion(question);
        state.setEnabled(true);
        roomLiveStore.savePressureState(roomId, state);
        return buildLiveStateResponse(roomId);
    }

    @Override
    public ChatMessageResponse sendChat(String roomId, String text) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        roomContextSupport.assertTrainingInProgress(ctx);
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        String trimmed = text.trim();
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("消息长度不能超过 500 字");
        }

        RoomChatMessage message = new RoomChatMessage();
        message.setFromRole(roomContextSupport.resolveRole(ctx));
        message.setFromUserId(UserContext.requireUserId());
        message.setText(trimmed);
        RoomChatMessage saved = roomLiveStore.appendChat(roomId, message);
        return toChatResponse(saved);
    }

    @Override
    public ChatMessagePageResponse listChat(String roomId, int page, int size) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        List<ChatMessageResponse> records =
                roomLiveStore.listChat(roomId, page, size).stream().map(this::toChatResponse).toList();
        return ChatMessagePageResponse.builder()
                .records(records)
                .total(records.size())
                .page(page)
                .size(size)
                .build();
    }

    @Override
    public RoomWhiteboardView toggleWhiteboard(String roomId, WhiteboardToggleRequest request) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        roomContextSupport.assertCoach(ctx);
        roomContextSupport.assertTrainingInProgress(ctx);

        RoomWhiteboardState state = roomLiveStore.getWhiteboardState(roomId);
        state.setActive(Boolean.TRUE.equals(request.getActive()));
        state.setVersion(state.getVersion() + 1);
        roomLiveStore.saveWhiteboardState(roomId, state);
        return toWhiteboardView(state);
    }

    @Override
    public RoomWhiteboardView appendWhiteboardStrokes(String roomId, WhiteboardStrokeBatchRequest request) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        roomContextSupport.assertCoach(ctx);
        roomContextSupport.assertTrainingInProgress(ctx);

        List<RoomWhiteboardStroke> strokes = request.getStrokes().stream().map(this::toStroke).toList();
        RoomWhiteboardState state = roomLiveStore.appendWhiteboardStrokes(roomId, strokes);
        return toWhiteboardView(state);
    }

    @Override
    public RoomWhiteboardView clearWhiteboard(String roomId) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        roomContextSupport.assertCoach(ctx);
        roomContextSupport.assertTrainingInProgress(ctx);

        RoomWhiteboardState state = roomLiveStore.clearWhiteboard(roomId);
        return toWhiteboardView(state);
    }

    private RoomLiveStateResponse buildLiveStateResponse(String roomId) {
        RoomPressureState state = roomLiveStore.getPressureState(roomId);
        Countdown countdown = state.getCountdown() != null ? state.getCountdown() : new Countdown();
        RoomCountdownView countdownView = resolveCountdownView(countdown);

        RoomInterruptView interruptView = null;
        if (state.getLastInterrupt() != null) {
            interruptView = RoomInterruptView.builder()
                    .message(state.getLastInterrupt().getMessage())
                    .at(state.getLastInterrupt().getAt())
                    .build();
        }

        RoomQuestionView questionView = null;
        if (state.getCurrentQuestion() != null) {
            questionView = RoomQuestionView.builder()
                    .questionId(state.getCurrentQuestion().getQuestionId())
                    .text(state.getCurrentQuestion().getText())
                    .build();
        }

        RoomPressureModeView pressureMode = RoomPressureModeView.builder()
                .enabled(state.isEnabled())
                .countdown(countdownView)
                .lastInterrupt(interruptView)
                .currentQuestion(questionView)
                .build();

        return RoomLiveStateResponse.builder()
                .pressureMode(pressureMode)
                .whiteboard(toWhiteboardView(roomLiveStore.getWhiteboardState(roomId)))
                .serverTime(LocalDateTime.now())
                .build();
    }

    private RoomWhiteboardView toWhiteboardView(RoomWhiteboardState state) {
        List<RoomWhiteboardStrokeView> strokes = state.getStrokes().stream().map(this::toStrokeView).toList();
        return RoomWhiteboardView.builder()
                .active(state.isActive())
                .version(state.getVersion())
                .strokes(strokes)
                .build();
    }

    private RoomWhiteboardStrokeView toStrokeView(RoomWhiteboardStroke stroke) {
        List<RoomWhiteboardStrokeView.PointView> points = stroke.getPoints().stream()
                .map(p -> RoomWhiteboardStrokeView.PointView.builder().x(p.getX()).y(p.getY()).build())
                .toList();
        return RoomWhiteboardStrokeView.builder()
                .id(stroke.getId())
                .color(stroke.getColor())
                .width(stroke.getWidth())
                .points(points)
                .build();
    }

    private RoomWhiteboardStroke toStroke(WhiteboardStrokeInput input) {
        RoomWhiteboardStroke stroke = new RoomWhiteboardStroke();
        stroke.setId(StringUtils.hasText(input.getId()) ? input.getId() : UUID.randomUUID().toString().replace("-", ""));
        stroke.setColor(StringUtils.hasText(input.getColor()) ? input.getColor() : "#ffffff");
        stroke.setWidth(input.getWidth() > 0 ? input.getWidth() : 3);
        stroke.setPoints(input.getPoints().stream().map(p -> {
            RoomWhiteboardStroke.Point pt = new RoomWhiteboardStroke.Point();
            pt.setX(p.getX());
            pt.setY(p.getY());
            return pt;
        }).toList());
        return stroke;
    }

    private RoomCountdownView resolveCountdownView(Countdown countdown) {
        if (countdown == null || !countdown.isActive()) {
            return RoomCountdownView.builder()
                    .active(false)
                    .secondsLeft(0)
                    .totalSeconds(countdown != null ? countdown.getTotalSeconds() : null)
                    .build();
        }
        int total = countdown.getTotalSeconds() != null ? countdown.getTotalSeconds() : 60;
        int left = total;
        if (countdown.getStartedAtEpochMs() != null) {
            long elapsed = (System.currentTimeMillis() - countdown.getStartedAtEpochMs()) / 1000;
            left = (int) Math.max(0, total - elapsed);
            if (left == 0) {
                return RoomCountdownView.builder()
                        .active(false)
                        .secondsLeft(0)
                        .totalSeconds(total)
                        .build();
            }
        }
        return RoomCountdownView.builder().active(true).secondsLeft(left).totalSeconds(total).build();
    }

    private ChatMessageResponse toChatResponse(RoomChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .fromRole(message.getFromRole())
                .fromUserId(message.getFromUserId())
                .text(message.getText())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
