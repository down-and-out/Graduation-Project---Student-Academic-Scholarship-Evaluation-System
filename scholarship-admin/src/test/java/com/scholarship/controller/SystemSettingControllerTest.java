package com.scholarship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarship.dto.AwardConfig;
import com.scholarship.dto.AwardRule;
import com.scholarship.dto.BasicSetting;
import com.scholarship.dto.WeightSetting;
import com.scholarship.entity.SysSetting;
import com.scholarship.service.SysSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SystemSettingController 系统设置控制器测试
 * <p>
 * 注意：本测试类使用 standalone MockMvc 设置，不测试 Spring Security 注解（@PreAuthorize）
 * 权限控制测试需要在集成测试中进行
 * </p>
 */
@DisplayName("SystemSettingController 系统设置控制器测试")
class SystemSettingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SysSettingService sysSettingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SystemSettingController controller = new SystemSettingController(sysSettingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ==================== 获取设置测试 ====================

    @Test
    @DisplayName("测试获取所有设置")
    void testGetAllSettings() throws Exception {
        Map<String, String> settings = Map.of(
                "basic", "{\"systemName\":\"测试系统\"}",
                "weight", "{\"courseWeight\":40}",
                "awards", "{\"version\":\"1.0\"}"
        );

        when(sysSettingService.getAllSettings()).thenReturn(settings);

        mockMvc.perform(get("/system/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.basic").exists())
                .andExpect(jsonPath("$.data.weight").exists())
                .andExpect(jsonPath("$.data.awards").exists());

        verify(sysSettingService).getAllSettings();
    }

    @Test
    @DisplayName("测试获取基本设置")
    void testGetBasicSetting() throws Exception {
        String basicJson = "{\"systemName\":\"研究生学业奖学金评定系统\",\"systemShortName\":\"奖学金评定系统\",\"currentSemester\":\"2025-1\",\"adminEmail\":\"admin@example.com\",\"adminPhone\":\"010-12345678\",\"announcement\":\"\"}";

        SysSetting setting = new SysSetting();
        setting.setSettingKey("basic");
        setting.setSettingValue(basicJson);

        when(sysSettingService.getByKey("basic")).thenReturn(setting);

        mockMvc.perform(get("/system/setting/basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试获取权重设置")
    void testGetWeightSetting() throws Exception {
        String weightJson = "{\"courseWeight\":40,\"researchWeight\":35,\"comprehensiveWeight\":25}";

        SysSetting setting = new SysSetting();
        setting.setSettingKey("weight");
        setting.setSettingValue(weightJson);

        when(sysSettingService.getByKey("weight")).thenReturn(setting);

        mockMvc.perform(get("/system/setting/weight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(weightJson));
    }

    @Test
    @DisplayName("测试获取奖项配置")
    void testGetAwardConfig() throws Exception {
        AwardConfig config = new AwardConfig();
        config.setVersion("1.0");
        config.setName("2025年奖项配置");
        config.setAllocationStrategy("scorePriority");

        AwardRule rule1 = new AwardRule();
        rule1.setId("award_1");
        rule1.setName("一等奖");
        rule1.setRatio(10);
        rule1.setAmount(10000);
        rule1.setPriority(1);

        AwardRule rule2 = new AwardRule();
        rule2.setId("award_2");
        rule2.setName("二等奖");
        rule2.setRatio(20);
        rule2.setAmount(8000);
        rule2.setPriority(2);

        config.setRules(List.of(rule1, rule2));

        String awardsJson = objectMapper.writeValueAsString(config);

        SysSetting setting = new SysSetting();
        setting.setSettingKey("awards");
        setting.setSettingValue(awardsJson);

        when(sysSettingService.getByKey("awards")).thenReturn(setting);

        mockMvc.perform(get("/system/setting/awards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试获取设置 - 设置不存在")
    void testGetSetting_NotFound() throws Exception {
        when(sysSettingService.getByKey("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/system/setting/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 更新设置测试 ====================
    // 注意：更新操作需要 Spring Security 的 @PreAuthorize 支持
    // 这些测试需要在集成测试环境中进行，standalone MockMvc 无法测试安全注解

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试获取所有设置 - 空数据")
    void testGetAllSettings_Empty() throws Exception {
        when(sysSettingService.getAllSettings()).thenReturn(Map.of());

        mockMvc.perform(get("/system/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ==================== 辅助方法 ====================

    private BasicSetting createBasicSetting() {
        BasicSetting setting = new BasicSetting();
        setting.setSystemName("研究生学业奖学金评定系统");
        setting.setSystemShortName("奖学金评定系统");
        setting.setCurrentSemester("2025-1");
        setting.setAdminEmail("admin@example.com");
        setting.setAdminPhone("010-12345678");
        setting.setAnnouncement("系统公告");
        return setting;
    }

    private AwardConfig createAwardConfig() {
        AwardConfig config = new AwardConfig();
        config.setVersion("1.0");
        config.setName("2025年奖项配置");
        config.setAllocationStrategy("scorePriority");

        AwardRule rule1 = new AwardRule();
        rule1.setId("award_1");
        rule1.setName("一等奖");
        rule1.setRatio(10);
        rule1.setAmount(10000);
        rule1.setScoreRange(new com.scholarship.dto.ScoreRange() {{ setMin(90.0); setMax(100.0); }});
        rule1.setConditions(List.of());
        rule1.setPriority(1);

        AwardRule rule2 = new AwardRule();
        rule2.setId("award_2");
        rule2.setName("二等奖");
        rule2.setRatio(20);
        rule2.setAmount(8000);
        rule2.setScoreRange(new com.scholarship.dto.ScoreRange() {{ setMin(80.0); setMax(89.99); }});
        rule2.setConditions(List.of());
        rule2.setPriority(2);

        AwardRule rule3 = new AwardRule();
        rule3.setId("award_3");
        rule3.setName("三等奖");
        rule3.setRatio(30);
        rule3.setAmount(5000);
        rule3.setScoreRange(new com.scholarship.dto.ScoreRange() {{ setMin(70.0); setMax(79.99); }});
        rule3.setConditions(List.of());
        rule3.setPriority(3);

        config.setRules(List.of(rule1, rule2, rule3));
        return config;
    }
}
