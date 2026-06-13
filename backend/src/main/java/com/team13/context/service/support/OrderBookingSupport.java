package com.team13.context.service.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.common.ResourceNotFoundException;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.dto.CreateOrderRequest;
import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.CoachScheduleSlot;
import com.team13.context.entity.CoachWeeklySchedule;
import com.team13.context.entity.Order;
import com.team13.context.entity.Product;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.CoachScheduleSlotMapper;
import com.team13.context.mapper.CoachWeeklyScheduleMapper;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OrderBookingSupport {

    public static final int PAY_EXPIRE_MINUTES = 15;
    public static final int DEFAULT_SESSION_MINUTES = 60;

    private static final List<Integer> MOCK_SLOT_HOURS = List.of(9, 10, 11, 14, 15, 16, 19, 20);

    private final CoachProfileMapper coachProfileMapper;
    private final CoachScheduleSlotMapper coachScheduleSlotMapper;
    private final CoachWeeklyScheduleMapper coachWeeklyScheduleMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public CoachProfile requireActiveCoach(Long coachId) {
        if (coachId == null) {
            throw new IllegalArgumentException("请选择陪练员");
        }
        CoachProfile profile = coachProfileMapper.selectById(coachId);
        if (profile == null || !Integer.valueOf(1).equals(profile.getStatus())) {
            throw new ResourceNotFoundException("陪练员不存在或已下架");
        }
        return profile;
    }

    public OrderScheduleSupport.Window resolveSchedule(CreateOrderRequest request) {
        return OrderScheduleSupport.resolve(
                request.getScheduledStart(),
                request.getScheduledEnd(),
                request.getSlotId(),
                request.getDate());
    }

    public void assertScheduleBookable(Long coachId, OrderScheduleSupport.Window window, Long excludeOrderId) {
        LocalDateTime now = LocalDateTime.now();
        if (window.start().isBefore(now.minusMinutes(5))) {
            throw new IllegalArgumentException("预约时段已过期，请重新选择");
        }
        if (hasCoachConflict(coachId, window.start(), window.end(), excludeOrderId)) {
            throw new IllegalArgumentException("该时段已被预约，请选择其他时间");
        }
    }

    public boolean isSlotBooked(Long coachId, LocalDateTime start, LocalDateTime end) {
        return hasCoachConflict(coachId, start, end, null);
    }

    public BigDecimal resolveOriginalAmount(Long coachId, Long productId) {
        if (productId != null) {
            Product product = productMapper.selectById(productId);
            if (product != null && Integer.valueOf(1).equals(product.getStatus())) {
                return product.getPrice().setScale(2, RoundingMode.HALF_UP);
            }
        }
        CoachProfile coach = requireActiveCoach(coachId);
        BigDecimal per30 = coach.getPricePer30m() != null ? coach.getPricePer30m() : BigDecimal.valueOf(99);
        return per30.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 将陪练端「周排班」同步为未来 14 天可预约时段（按小时切分，供学员端展示）。
     */
    public void syncFutureSlotsFromWeekly(Long coachId) {
        coachScheduleSlotMapper.delete(
                Wrappers.<CoachScheduleSlot>lambdaQuery()
                        .eq(CoachScheduleSlot::getCoachId, coachId)
                        .ge(CoachScheduleSlot::getStartTime, LocalDate.now().atStartOfDay()));
        List<CoachWeeklySchedule> weekly = coachWeeklyScheduleMapper.selectList(
                Wrappers.<CoachWeeklySchedule>lambdaQuery()
                        .eq(CoachWeeklySchedule::getCoachId, coachId)
                        .eq(CoachWeeklySchedule::getEnabled, 1));
        LocalDateTime now = LocalDateTime.now();
        for (int dayOffset = 0; dayOffset < 14; dayOffset++) {
            LocalDate day = LocalDate.now().plusDays(dayOffset);
            int dow = day.getDayOfWeek().getValue() % 7;
            for (CoachWeeklySchedule w : weekly) {
                if (!Objects.equals(w.getDayOfWeek(), dow)) {
                    continue;
                }
                LocalDateTime rangeStart = LocalDateTime.of(day, w.getStartTime());
                LocalDateTime rangeEnd = LocalDateTime.of(day, w.getEndTime());
                if (!rangeEnd.isAfter(rangeStart)) {
                    continue;
                }
                LocalDateTime cursor = rangeStart;
                while (cursor.isBefore(rangeEnd)) {
                    LocalDateTime slotEnd = cursor.plusHours(1);
                    if (slotEnd.isAfter(rangeEnd)) {
                        slotEnd = rangeEnd;
                    }
                    CoachScheduleSlot slot = new CoachScheduleSlot();
                    slot.setCoachId(coachId);
                    slot.setStartTime(cursor);
                    slot.setEndTime(slotEnd);
                    slot.setStatus(1);
                    slot.setCreatedAt(now);
                    slot.setUpdatedAt(now);
                    coachScheduleSlotMapper.insert(slot);
                    cursor = slotEnd;
                }
            }
        }
    }

    public List<Map<String, Object>> buildSlotOptions(Long coachId, LocalDate day) {
        List<CoachScheduleSlot> dbSlots = listOpenSlots(coachId, day);
        if (!dbSlots.isEmpty()) {
            List<Map<String, Object>> items = expandDbSlotsToOptions(coachId, dbSlots);
            if (!items.isEmpty()) {
                return items;
            }
        }
        List<Map<String, Object>> items = new ArrayList<>();
        List<Integer> hours = mockSlotHours();
        for (int i = 0; i < hours.size(); i++) {
            int hour = hours.get(i);
            LocalDateTime start = day.atTime(hour, 0);
            LocalDateTime end = start.plusHours(1);
            if (start.isBefore(LocalDateTime.now().minusMinutes(5))) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.format(Locale.ROOT, "slot-%s-%d", day, i));
            item.put("label", formatSlotLabel(start, end));
            item.put("booked", isSlotBooked(coachId, start, end));
            items.add(item);
        }
        return items;
    }

    public BigDecimal resolvePayableAmount(
            Long coachId, Long productId, BigDecimal clientAmount, String couponId) {
        BigDecimal original = resolveOriginalAmount(coachId, productId);
        return CouponCatalog.applyDiscount(original, couponId);
    }

    private List<Map<String, Object>> expandDbSlotsToOptions(Long coachId, List<CoachScheduleSlot> dbSlots) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<Map<String, Object>> items = new ArrayList<>();
        for (CoachScheduleSlot slot : dbSlots) {
            LocalDateTime cursor = slot.getStartTime();
            LocalDateTime rangeEnd = slot.getEndTime();
            while (cursor.isBefore(rangeEnd)) {
                LocalDateTime segEnd = cursor.plusHours(1);
                if (segEnd.isAfter(rangeEnd)) {
                    segEnd = rangeEnd;
                }
                if (!segEnd.isBefore(cutoff) && segEnd.isAfter(cursor)) {
                    LocalDateTime bookStart = cursor.isBefore(cutoff) ? alignNextSegmentStart(cutoff) : cursor;
                    if (bookStart.isBefore(segEnd)) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("id", formatSlotId(bookStart));
                        item.put("label", formatSlotLabel(bookStart, segEnd));
                        item.put("booked", isSlotBooked(coachId, bookStart, segEnd));
                        items.add(item);
                    }
                }
                cursor = segEnd;
            }
        }
        return items;
    }

    private static LocalDateTime alignNextSegmentStart(LocalDateTime cutoff) {
        LocalDateTime hourStart = cutoff.withMinute(0).withSecond(0).withNano(0);
        if (hourStart.isBefore(cutoff)) {
            hourStart = hourStart.plusHours(1);
        }
        return hourStart;
    }

    public List<CoachScheduleSlot> listOpenSlots(Long coachId, LocalDate day) {
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();
        return coachScheduleSlotMapper.selectList(
                Wrappers.<CoachScheduleSlot>lambdaQuery()
                        .eq(CoachScheduleSlot::getCoachId, coachId)
                        .eq(CoachScheduleSlot::getStatus, 1)
                        .ge(CoachScheduleSlot::getStartTime, dayStart)
                        .lt(CoachScheduleSlot::getStartTime, dayEnd)
                        .orderByAsc(CoachScheduleSlot::getStartTime));
    }

    public String formatSlotId(LocalDateTime start) {
        return String.format(Locale.ROOT, "%04d-%02d-%02d-%d", start.getYear(), start.getMonthValue(), start.getDayOfMonth(), start.getHour());
    }

    public String formatSlotLabel(LocalDateTime start, LocalDateTime end) {
        return String.format(Locale.ROOT, "%02d:%02d-%02d:%02d", start.getHour(), start.getMinute(), end.getHour(), end.getMinute());
    }

    public LocalDateTime payExpireAt(LocalDateTime createdAt) {
        return createdAt != null ? createdAt.plusMinutes(PAY_EXPIRE_MINUTES) : null;
    }

    public boolean isPayExpired(Order order, LocalDateTime now) {
        if (order == null || !BusinessStatuses.Order.PENDING_PAY.equals(order.getStatus())) {
            return false;
        }
        LocalDateTime expireAt = payExpireAt(order.getCreatedAt());
        return expireAt != null && now.isAfter(expireAt);
    }

    private boolean hasCoachConflict(Long coachId, LocalDateTime start, LocalDateTime end, Long excludeOrderId) {
        return orderMapper.selectCount(
                        Wrappers.<Order>lambdaQuery()
                                .eq(Order::getCoachId, coachId)
                                .in(Order::getStatus,
                                        BusinessStatuses.Order.PENDING_PAY,
                                        BusinessStatuses.Order.PAID,
                                        BusinessStatuses.Order.IN_SERVICE)
                                .lt(Order::getScheduledStart, end)
                                .gt(Order::getScheduledEnd, start)
                                .ne(excludeOrderId != null, Order::getId, excludeOrderId))
                > 0;
    }

    static List<Integer> mockSlotHours() {
        return MOCK_SLOT_HOURS;
    }
}
