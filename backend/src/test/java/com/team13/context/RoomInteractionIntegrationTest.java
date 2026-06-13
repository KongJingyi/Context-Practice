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

class RoomInteractionIntegrationTest extends AbstractMysqlIntegrationTest {

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
    void pressureAndChat_coachControlsUserReceivesViaState() throws Exception {
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
                .andReturn();
        String roomId = objectMapper.readTree(started.getResponse().getContentAsString())
                .path("data").path("roomId").asText();

        mockMvc.perform(post("/api/v1/rooms/" + roomId + "/pressure/countdown")
                        .header("Authorization", authorizationHeader(coach.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"start\",\"seconds\":60}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.secondsLeft").value(60));

        mockMvc.perform(post("/api/v1/rooms/" + roomId + "/pressure/interrupt")
                        .header("Authorization", authorizationHeader(coach.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"请直接给结论\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pressureMode.lastInterrupt.message").value("请直接给结论"));

        mockMvc.perform(get("/api/v1/rooms/" + roomId + "/state")
                        .header("Authorization", authorizationHeader(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pressureMode.enabled").value(true))
                .andExpect(jsonPath("$.data.pressureMode.countdown.active").value(true));

        mockMvc.perform(post("/api/v1/rooms/" + roomId + "/chat")
                        .header("Authorization", authorizationHeader(coach.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"你好，我们先从自我介绍开始\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.text").value("你好，我们先从自我介绍开始"));

        MvcResult chatList = mockMvc.perform(get("/api/v1/rooms/" + roomId + "/chat")
                        .header("Authorization", authorizationHeader(user.getId())))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode records = objectMapper.readTree(chatList.getResponse().getContentAsString())
                .path("data").path("records");
        assertThat(records.isArray()).isTrue();
        assertThat(records.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void pressureCountdown_forbiddenForUser() throws Exception {
        User user = insertUser();
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

        mockMvc.perform(post("/api/v1/rooms/" + roomId + "/pressure/countdown")
                        .header("Authorization", authorizationHeader(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"start\",\"seconds\":30}"))
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
