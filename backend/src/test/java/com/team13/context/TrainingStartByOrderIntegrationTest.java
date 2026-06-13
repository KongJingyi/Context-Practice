package com.team13.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team13.context.entity.User;
import com.team13.context.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainingStartByOrderIntegrationTest extends AbstractMysqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void getStart_rejectsWhenOrderNotPaid() throws Exception {
        User user = insertUser();

        String createBody = """
                {"amount":10.00}
                """;
        MvcResult created = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", authorizationHeader(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        long orderId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("orderId").asLong();

        mockMvc.perform(get("/api/training/start")
                        .param("orderId", String.valueOf(orderId))
                        .header("Authorization", authorizationHeader(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getStart_returnsRoom_whenOrderPaid() throws Exception {
        User user = insertUser();

        String createBody = """
                {"productId":null,"coachId":null,"sceneId":null,"amount":12.34}
                """;
        MvcResult created = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", authorizationHeader(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();
        long orderId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("orderId").asLong();

        String payBody = """
                {"orderId":%d}
                """.formatted(orderId);
        mockMvc.perform(post("/api/v1/orders/mock-pay")
                        .header("Authorization", authorizationHeader(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult started = mockMvc.perform(get("/api/training/start")
                        .param("orderId", String.valueOf(orderId))
                        .header("Authorization", authorizationHeader(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roomId").exists())
                .andReturn();
        String roomId = objectMapper.readTree(started.getResponse().getContentAsString())
                .path("data").path("roomId").asText();
        assertThat(roomId).isNotBlank();
    }

    @Test
    void mockPay_rejectsWhenOrderBelongsToAnotherUser() throws Exception {
        User owner = insertUser();
        User attacker = insertUser();

        MvcResult created = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", authorizationHeader(owner.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10.00}"))
                .andExpect(status().isOk())
                .andReturn();
        long orderId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("orderId").asLong();

        mockMvc.perform(post("/api/v1/orders/mock-pay")
                        .header("Authorization", authorizationHeader(attacker.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":" + orderId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无权支付该订单"));
    }

    private User insertUser() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername("u_" + UUID.randomUUID());
        user.setStatus(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return user;
    }
}
