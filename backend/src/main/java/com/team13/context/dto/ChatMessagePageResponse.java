package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatMessagePageResponse {

    private List<ChatMessageResponse> records;
    private long total;
    private int page;
    private int size;
}
