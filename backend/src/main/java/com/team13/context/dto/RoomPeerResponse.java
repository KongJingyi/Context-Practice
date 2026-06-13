package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomPeerResponse {

    private Long userId;
    private String trtcUserId;
    private String nickname;
    private String avatar;
}
