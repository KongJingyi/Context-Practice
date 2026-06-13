package com.team13.context.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.room")
public class RoomProperties {

    /** 预约开始前允许进房的分钟数 */
    private int earlyEnterMinutes = 5;

    /** 预约结束后允许进房的宽限分钟数 */
    private int graceAfterEndMinutes = 15;
}
