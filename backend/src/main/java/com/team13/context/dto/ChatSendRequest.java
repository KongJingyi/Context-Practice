package com.team13.context.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatSendRequest {

    @NotBlank
    private String text;
}
