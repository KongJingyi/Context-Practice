package com.team13.context.service;

import com.team13.context.dto.ChatMessagePageResponse;
import com.team13.context.dto.ChatMessageResponse;
import com.team13.context.dto.PressureCountdownRequest;
import com.team13.context.dto.PressureCountdownResponse;
import com.team13.context.dto.PressureInterruptRequest;
import com.team13.context.dto.PressureQuestionRequest;
import com.team13.context.dto.RoomLiveStateResponse;
import com.team13.context.dto.RoomWhiteboardView;
import com.team13.context.dto.WhiteboardStrokeBatchRequest;
import com.team13.context.dto.WhiteboardToggleRequest;

public interface RoomInteractionService {

    RoomLiveStateResponse getLiveState(String roomId);

    PressureCountdownResponse controlCountdown(String roomId, PressureCountdownRequest request);

    RoomLiveStateResponse interrupt(String roomId, PressureInterruptRequest request);

    RoomLiveStateResponse askQuestion(String roomId, PressureQuestionRequest request);

    ChatMessageResponse sendChat(String roomId, String text);

    ChatMessagePageResponse listChat(String roomId, int page, int size);

    RoomWhiteboardView toggleWhiteboard(String roomId, WhiteboardToggleRequest request);

    RoomWhiteboardView appendWhiteboardStrokes(String roomId, WhiteboardStrokeBatchRequest request);

    RoomWhiteboardView clearWhiteboard(String roomId);
}
