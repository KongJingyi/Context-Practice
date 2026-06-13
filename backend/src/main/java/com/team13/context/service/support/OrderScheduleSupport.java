package com.team13.context.service.support;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将前端预约字段（slotId / date / ISO 时间）解析为订单 scheduledStart / scheduledEnd。
 */
public final class OrderScheduleSupport {

    private static final Pattern SLOT_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})-(\\d{1,2})$");
    private static final Pattern SLOT_PREFIX_PATTERN =
            Pattern.compile("^slot-(\\d{4}-\\d{2}-\\d{2})-(\\d+)$", Pattern.CASE_INSENSITIVE);

    /** 前端 mock 时段序号 → 小时 */
    private static final int[] MOCK_SLOT_HOURS = {9, 10, 11, 14, 15, 16, 19, 20};

    private OrderScheduleSupport() {
    }

    public record Window(LocalDateTime start, LocalDateTime end) {
    }

    public static Window resolve(
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd,
            String slotId,
            String date) {
        if (scheduledStart != null) {
            LocalDateTime end = scheduledEnd != null ? scheduledEnd : scheduledStart.plusHours(1);
            return new Window(scheduledStart, end);
        }
        Window fromSlot = parseSlotId(slotId);
        if (fromSlot != null) {
            return fromSlot;
        }
        if (StringUtils.hasText(date)) {
            LocalDate day = LocalDate.parse(date.trim());
            LocalDateTime start = day.atStartOfDay().plusHours(9);
            return new Window(start, start.plusHours(1));
        }
        LocalDateTime now = LocalDateTime.now();
        return new Window(now.minusMinutes(1), now.plusHours(1));
    }

    static Window parseSlotId(String slotId) {
        if (!StringUtils.hasText(slotId)) {
            return null;
        }
        String trimmed = slotId.trim();
        Matcher matcher = SLOT_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            return windowFromDayHour(matcher.group(1), Integer.parseInt(matcher.group(2)));
        }
        Matcher prefixed = SLOT_PREFIX_PATTERN.matcher(trimmed);
        if (prefixed.matches()) {
            int tail = Integer.parseInt(prefixed.group(2));
            if (tail >= 0 && tail < MOCK_SLOT_HOURS.length) {
                return windowFromDayHour(prefixed.group(1), MOCK_SLOT_HOURS[tail]);
            }
            return windowFromDayHour(prefixed.group(1), tail);
        }
        return null;
    }

    private static Window windowFromDayHour(String dayText, int hour) {
        LocalDate day = LocalDate.parse(dayText);
        LocalDateTime start = day.atTime(hour, 0);
        return new Window(start, start.plusHours(1));
    }
}
