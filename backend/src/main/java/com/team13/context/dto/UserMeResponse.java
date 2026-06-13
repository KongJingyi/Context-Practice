package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserMeResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private List<String> roles;
    private String phone;
}
