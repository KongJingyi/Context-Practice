package com.team13.context.order;

import com.team13.context.service.support.OrderScheduleSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderScheduleSupportTest {

    @Test
    void resolve_parsesSlotId() {
        OrderScheduleSupport.Window window =
                OrderScheduleSupport.resolve(null, null, "2026-06-05-14", null);
        assertEquals(LocalDateTime.of(2026, 6, 5, 14, 0), window.start());
        assertEquals(LocalDateTime.of(2026, 6, 5, 15, 0), window.end());
    }

    @Test
    void resolve_prefersExplicitSchedule() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 5, 10, 30);
        LocalDateTime end = LocalDateTime.of(2026, 6, 5, 11, 30);
        OrderScheduleSupport.Window window =
                OrderScheduleSupport.resolve(start, end, "2026-06-05-14", "2026-06-05");
        assertEquals(start, window.start());
        assertEquals(end, window.end());
    }

    @Test
    void resolve_parsesPrefixedMockSlotId() {
        OrderScheduleSupport.Window window =
                OrderScheduleSupport.resolve(null, null, "slot-2026-06-05-2", null);
        assertEquals(LocalDateTime.of(2026, 6, 5, 11, 0), window.start());
        assertEquals(LocalDateTime.of(2026, 6, 5, 12, 0), window.end());
    }
}
