package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomQuestionView {

    private Long questionId;
    private String text;
}
