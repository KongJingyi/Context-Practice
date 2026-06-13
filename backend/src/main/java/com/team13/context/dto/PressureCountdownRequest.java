package com.team13.context.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PressureCountdownRequest {

    /** start / stop / reset */
    @NotBlank
    private String action;

    private Integer seconds;
}
