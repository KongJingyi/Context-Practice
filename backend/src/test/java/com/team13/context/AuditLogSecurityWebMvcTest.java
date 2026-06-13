package com.team13.context;

import com.team13.context.common.GlobalExceptionHandler;
import com.team13.context.config.AuthProperties;
import com.team13.context.config.TestMethodSecurityConfig;
import com.team13.context.controller.AdminPortalController;
import com.team13.context.metrics.BusinessMetricsRecorder;
import com.team13.context.service.frontend.AdminPortalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminPortalController.class)
@Import({TestMethodSecurityConfig.class, GlobalExceptionHandler.class, AuthProperties.class})
@ActiveProfiles("test")
class AuditLogSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessMetricsRecorder businessMetricsRecorder;

    @MockBean
    private AdminPortalService adminPortalService;

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCannotListAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanListAuditLogs() throws Exception {
        when(adminPortalService.listAuditLogs(anyInt(), anyInt())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
