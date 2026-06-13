package com.team13.context.service.support;

import com.team13.context.entity.Order;
import com.team13.context.entity.TrainingRecord;

public record RoomContext(TrainingRecord training, Order order) {
}
