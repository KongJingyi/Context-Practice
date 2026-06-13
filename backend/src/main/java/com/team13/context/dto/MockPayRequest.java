package com.team13.context.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MockPayRequest {

    @NotNull
    private Long orderId;
}
