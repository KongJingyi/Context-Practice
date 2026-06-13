package com.team13.context.dto;

import lombok.Data;

@Data
public class RoomLeaveRequest {

    /** USER_HANGUP / COACH_HANGUP / NETWORK_ERROR / PAGE_UNLOAD */
    private String reason;
}
