package com.scholarship.controller;

import com.scholarship.config.SystemConfig;
import com.scholarship.dto.BasicSetting;
import com.scholarship.service.SysSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SystemSettingController tests")
class SystemSettingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SysSettingService sysSettingService;
    @Mock
    private SystemConfig systemConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemSettingController(sysSettingService, systemConfig)).build();
    }

    @Test
    void getAllSettingsReturnsMap() throws Exception {
        when(sysSettingService.getAllSettings()).thenReturn(Map.of("basic", "{}"));

        mockMvc.perform(get("/system/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.basic").value("{}"));
    }

    @Test
    void getBasicSettingReturnsTypedPayload() throws Exception {
        BasicSetting basicSetting = new BasicSetting();
        basicSetting.setSystemName("test");
        when(sysSettingService.getBasicSetting()).thenReturn(basicSetting);

        mockMvc.perform(get("/system/setting/basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.systemName").value("test"));
    }
}
