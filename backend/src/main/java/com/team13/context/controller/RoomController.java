package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.dto.ChatMessagePageResponse;
import com.team13.context.dto.ChatMessageResponse;
import com.team13.context.dto.ChatSendRequest;
import com.team13.context.dto.PressureCountdownRequest;
import com.team13.context.dto.PressureCountdownResponse;
import com.team13.context.dto.PressureInterruptRequest;
import com.team13.context.dto.PressureQuestionRequest;
import com.team13.context.dto.RoomDetailResponse;
import com.team13.context.dto.RoomEndRequest;
import com.team13.context.dto.RoomEndResponse;
import com.team13.context.dto.RoomJoinInfoResponse;
import com.team13.context.dto.RoomJoinResponse;
import com.team13.context.dto.RoomLeaveRequest;
import com.team13.context.dto.RoomLeaveResponse;
import com.team13.context.dto.RoomLiveStateResponse;
import com.team13.context.dto.RoomWhiteboardView;
import com.team13.context.dto.WhiteboardStrokeBatchRequest;
import com.team13.context.dto.WhiteboardToggleRequest;
import com.team13.context.service.RoomInteractionService;
import com.team13.context.service.RoomService;
import com.team13.context.service.frontend.CoachPortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomInteractionService roomInteractionService;
    private final CoachPortalService coachPortalService;

    @GetMapping("/{roomId}/join-info")
    public ApiResult<RoomJoinInfoResponse> joinInfo(@PathVariable String roomId) {
        return ApiResult.ok(roomService.getJoinInfo(roomId));
    }

    @PostMapping("/{roomId}/join")
    public ApiResult<RoomJoinResponse> join(@PathVariable String roomId) {
        return ApiResult.ok(roomService.joinRoom(roomId));
    }

    @PostMapping("/{roomId}/leave")
    public ApiResult<RoomLeaveResponse> leave(
            @PathVariable String roomId, @RequestBody(required = false) RoomLeaveRequest request) {
        return ApiResult.ok(roomService.leaveRoom(roomId, request));
    }

    @PostMapping("/{roomId}/end")
    public ApiResult<RoomEndResponse> end(
            @PathVariable String roomId, @RequestBody(required = false) RoomEndRequest request) {
        return ApiResult.ok(roomService.endRoom(roomId, request));
    }

    @GetMapping("/{roomId}")
    public ApiResult<RoomDetailResponse> detail(@PathVariable String roomId) {
        return ApiResult.ok(roomService.getRoomDetail(roomId));
    }

    @GetMapping("/{roomId}/state")
    public ApiResult<RoomLiveStateResponse> liveState(@PathVariable String roomId) {
        return ApiResult.ok(roomInteractionService.getLiveState(roomId));
    }

    @PostMapping("/{roomId}/pressure/countdown")
    public ApiResult<PressureCountdownResponse> pressureCountdown(
            @PathVariable String roomId, @Valid @RequestBody PressureCountdownRequest request) {
        return ApiResult.ok(roomInteractionService.controlCountdown(roomId, request));
    }

    @PostMapping("/{roomId}/pressure/interrupt")
    public ApiResult<RoomLiveStateResponse> pressureInterrupt(
            @PathVariable String roomId, @Valid @RequestBody PressureInterruptRequest request) {
        return ApiResult.ok(roomInteractionService.interrupt(roomId, request));
    }

    @PostMapping("/{roomId}/pressure/question")
    public ApiResult<RoomLiveStateResponse> pressureQuestion(
            @PathVariable String roomId, @Valid @RequestBody PressureQuestionRequest request) {
        return ApiResult.ok(roomInteractionService.askQuestion(roomId, request));
    }

    @PostMapping("/{roomId}/chat")
    public ApiResult<ChatMessageResponse> sendChat(
            @PathVariable String roomId, @Valid @RequestBody ChatSendRequest request) {
        return ApiResult.ok(roomInteractionService.sendChat(roomId, request.getText()));
    }

    @GetMapping("/{roomId}/chat")
    public ApiResult<ChatMessagePageResponse> listChat(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResult.ok(roomInteractionService.listChat(roomId, page, size));
    }

    @PostMapping("/{roomId}/whiteboard/toggle")
    public ApiResult<RoomWhiteboardView> whiteboardToggle(
            @PathVariable String roomId, @Valid @RequestBody WhiteboardToggleRequest request) {
        return ApiResult.ok(roomInteractionService.toggleWhiteboard(roomId, request));
    }

    @PostMapping("/{roomId}/whiteboard/strokes")
    public ApiResult<RoomWhiteboardView> whiteboardStrokes(
            @PathVariable String roomId, @Valid @RequestBody WhiteboardStrokeBatchRequest request) {
        return ApiResult.ok(roomInteractionService.appendWhiteboardStrokes(roomId, request));
    }

    @PostMapping("/{roomId}/whiteboard/clear")
    public ApiResult<RoomWhiteboardView> whiteboardClear(@PathVariable String roomId) {
        return ApiResult.ok(roomInteractionService.clearWhiteboard(roomId));
    }

    @GetMapping("/{roomId}/materials")
    public ApiResult<List<Map<String, Object>>> listMaterials(@PathVariable String roomId) {
        return ApiResult.ok(coachPortalService.listRoomMaterials(roomId));
    }

    @PostMapping("/{roomId}/materials")
    public ApiResult<Map<String, Object>> uploadMaterial(
            @PathVariable String roomId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ApiResult.ok(coachPortalService.uploadRoomMaterial(roomId, file));
    }
}
