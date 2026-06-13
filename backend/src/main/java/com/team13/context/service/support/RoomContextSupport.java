package com.team13.context.service.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.common.ForbiddenOperationException;
import com.team13.context.common.ResourceNotFoundException;
import com.team13.context.common.UserContext;
import com.team13.context.constant.ParticipantRoles;
import com.team13.context.entity.Order;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RoomContextSupport {

    private final TrainingRecordMapper trainingRecordMapper;
    private final OrderMapper orderMapper;

    public RoomContext loadRoomContext(String roomId) {
        TrainingRecord training = trainingRecordMapper.selectOne(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getRoomId, roomId)
                        .last("LIMIT 1"));
        if (training == null) {
            throw new ResourceNotFoundException("房间不存在");
        }
        Order order = training.getOrderId() != null ? orderMapper.selectById(training.getOrderId()) : null;
        if (order == null) {
            throw new ResourceNotFoundException("房间关联订单不存在");
        }
        return new RoomContext(training, order);
    }

    public void assertParticipant(RoomContext ctx) {
        Long currentUserId = UserContext.requireUserId();
        boolean isUser = Objects.equals(ctx.order().getUserId(), currentUserId);
        boolean isCoach = Objects.equals(ctx.order().getCoachId(), currentUserId);
        if (!isUser && !isCoach) {
            throw new ForbiddenOperationException("无权进入该训练房间");
        }
    }

    public String resolveRole(RoomContext ctx) {
        Long currentUserId = UserContext.requireUserId();
        if (Objects.equals(ctx.order().getCoachId(), currentUserId)) {
            return ParticipantRoles.COACH;
        }
        return ParticipantRoles.USER;
    }

    public void assertCoach(RoomContext ctx) {
        if (!ParticipantRoles.COACH.equals(resolveRole(ctx))) {
            throw new ForbiddenOperationException("仅陪练员可操作");
        }
    }

    public void assertTrainingInProgress(RoomContext ctx) {
        if (ctx.training() == null) {
            throw new IllegalArgumentException("训练尚未开始");
        }
    }
}
