package com.team13.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthPhoneWebMvcTest extends AbstractMysqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAndLoginByPhone_withDevFixedCode() throws Exception {
        String phone = "139" + String.format("%08d", Math.abs((int) (System.nanoTime() % 100_000_000)));

        mockMvc.perform(post("/api/v1/auth/register/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"%s","smsCode":"888888"}
                                """.formatted(phone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());

        mockMvc.perform(post("/api/v1/auth/login/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"%s","smsCode":"888888"}
                                """.formatted(phone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    void captchaEndpointReturnsImage() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.captchaKey").exists())
                .andReturn();
        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Map.class);
        Map<?, ?> data = (Map<?, ?>) body.get("data");
        assertThat(String.valueOf(data.get("captchaImage"))).contains("image/png");
    }
}
