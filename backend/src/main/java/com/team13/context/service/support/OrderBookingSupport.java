package com.team13.context.service.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.common.ResourceNotFoundException;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.dto.CreateOrderRequest;
import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.CoachScheduleSlot;
import com.team13.context.entity.Order;
import com.team13.context.entity.Product;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.CoachScheduleSlotMapper;
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

@Component
@RequiredArgsConstructor
public class OrderBookingSupport {

    public static final int PAY_EXPIRE_MINUTES = 15;
    public static final int DEFAULT_SESSION_MINUTES = 60;

    private static final List<Integer> MOCK_SLOT_HOURS = List.of(9, 10, 11, 14, 15, 16, 19, 20);

    private final CoachProfileMapper coachProfileMapper;
    private final CoachScheduleSlotMapper coachScheduleSlotMapper;
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

    public List<Map<String, Object>> buildSlotOptions(Long coachId, LocalDate day) {
        List<CoachScheduleSlot> dbSlots = listOpenSlots(coachId, day);
        if (!dbSlots.isEmpty()) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (CoachScheduleSlot slot : dbSlots) {
                if (slot.getStartTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", formatSlotId(slot.getStartTime()));
                item.put("label", formatSlotLabel(slot.getStartTime(), slot.getEndTime()));
                item.put("booked", isSlotBooked(coachId, slot.getStartTime(), slot.getEndTime()));
                items.add(item);
            }
            return items;
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
