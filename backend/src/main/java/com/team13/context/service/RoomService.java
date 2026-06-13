package com.team13.context.service;

import com.team13.context.dto.RoomDetailResponse;
import com.team13.context.dto.RoomEndRequest;
import com.team13.context.dto.RoomEndResponse;
import com.team13.context.dto.RoomJoinInfoResponse;
import com.team13.context.dto.RoomJoinResponse;
import com.team13.context.dto.RoomLeaveRequest;
import com.team13.context.dto.RoomLeaveResponse;

public interface RoomService {

    RoomJoinInfoResponse getJoinInfo(String roomId);

    RoomJoinResponse joinRoom(String roomId);

    RoomLeaveResponse leaveRoom(String roomId, RoomLeaveRequest request);

    RoomEndResponse endRoom(String roomId, RoomEndRequest request);

    RoomDetailResponse getRoomDetail(String roomId);
}
