package com.team13.context;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team13.context.entity.Order;
import com.team13.context.entity.Role;
import com.team13.context.entity.User;
import com.team13.context.entity.UserRole;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.RoleMapper;
import com.team13.context.mapper.UserMapper;
import com.team13.context.mapper.UserRoleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoomFlowIntegrationTest extends AbstractMysqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void roomFlow_userAndCoachJoin_thenCoachEnds() throws Exception {
        User user = insertUser();
        User coach = insertUser();
        assignRole(coach.getId(), "COACH");

        LocalDateTime now = LocalDateTime.now();
        Order order = new Order();
        order.setUserId(user.getId());
        order.setCoachId(coach.getId());
        order.setAmount(new BigDecimal("99.00"));
        order.setStatus("PAID");
        order.setScheduledStart(now.minusMinutes(1));
        order.setScheduledEnd(now.plusHours(1));
        order.setPlatformFeeRate(BigDecimal.ZERO);
        order.setPlatformFeeAmount(BigDecimal.ZERO);
        order.setCoachIncomeAmount(BigDecimal.ZERO);
        order.setPayAt(now);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderMapper.insert(order);

        MvcResult started = mockMvc.perform(get("/api/training/start")
                        .param("orderId", String.valueOf(order.getId()))
                        .header("Authorization", authorizationHeader(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roomId").exists())
                .andExpect(jsonPath("$.data.trainingId").exists())
                .andReturn();
        JsonNode startData = objectMapper.readTree(started.getResponse().getContentAsString()).path("data");
        String roomId = startData.path("roomId").asText();

        mockMvc.perform(get("/api/v1/rooms/" + roomId + "/join-info")
                        .header("Authorization", authorizationHeader(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.canEnter").value(true))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.trtcUserId").value("u_" + user.getId()))
                .andExpect(jsonPath("$.data.userSig").exists());

        mockMvc.perform(post("/api/v1/rooms/" + roomId + "/join")
                        .header("Authorization", authorizationHeader(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("USER"));

        mockMvc.perform(get("/api/v1/rooms/" + roomId + "/join-info")
                        .header("Authorization", authorizationHeader(coach.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("COACH"))
                .andExpect(jsonPath("$.data.trtcUserId").value("c_" + coach.getId()));

        mockMvc.perform(post("/api/v1/rooms/" + roomId + "/join")
                        .header("Authorization", authorizationHeader(coach.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("COACH"));

        mockMvc.perform(get("/api/v1/rooms/" + roomId)
                        .header("Authorization", authorizationHeader(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.participants[?(@.role=='USER')].joined").value(true))
                .andExpect(jsonPath("$.data.participants[?(@.role=='COACH')].joined").value(true));

        mockMvc.perform(post("/api/v1/rooms/" + roomId + "/end")
                        .header("Authorization", authorizationHeader(coach.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderStatus").value("COMPLETED"));

        Order refreshed = orderMapper.selectById(order.getId());
        assertThat(refreshed.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void joinInfo_forbiddenForUnrelatedUser() throws Exception {
        User user = insertUser();
        User stranger = insertUser();
        User coach = insertUser();
        assignRole(coach.getId(), "COACH");

        LocalDateTime now = LocalDateTime.now();
        Order order = new Order();
        order.setUserId(user.getId());
        order.setCoachId(coach.getId());
        order.setAmount(new BigDecimal("50.00"));
        order.setStatus("PAID");
        order.setScheduledStart(now.minusMinutes(1));
        order.setScheduledEnd(now.plusHours(1));
        order.setPlatformFeeRate(BigDecimal.ZERO);
        order.setPlatformFeeAmount(BigDecimal.ZERO);
        order.setCoachIncomeAmount(BigDecimal.ZERO);
        order.setPayAt(now);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderMapper.insert(order);

        MvcResult started = mockMvc.perform(get("/api/training/start")
                        .param("orderId", String.valueOf(order.getId()))
                        .header("Authorization", authorizationHeader(user.getId())))
                .andReturn();
        String roomId = objectMapper.readTree(started.getResponse().getContentAsString())
                .path("data").path("roomId").asText();

        mockMvc.perform(get("/api/v1/rooms/" + roomId + "/join-info")
                        .header("Authorization", authorizationHeader(stranger.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    private User insertUser() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername("u_" + UUID.randomUUID());
        user.setStatus(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        assignRole(user.getId(), "USER");
        return user;
    }

    private void assignRole(Long userId, String roleCode) {
        Role role = roleMapper.selectOne(
                Wrappers.<Role>lambdaQuery().eq(Role::getCode, roleCode).last("LIMIT 1"));
        if (role == null) {
            return;
        }
        Long count = userRoleMapper.selectCount(
                Wrappers.<UserRole>lambdaQuery()
                        .eq(UserRole::getUserId, userId)
                        .eq(UserRole::getRoleId, role.getId()));
        if (count != null && count > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRole.setCreatedAt(now);
        userRole.setUpdatedAt(now);
        userRoleMapper.insert(userRole);
    }
}
