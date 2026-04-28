package com.scholarship.service.impl;

import com.alibaba.fastjson2.JSON;
import com.scholarship.dto.AwardConfig;
import com.scholarship.dto.AwardRule;
import com.scholarship.dto.BasicSetting;
import com.scholarship.dto.WeightSetting;
import com.scholarship.entity.SysSetting;
import com.scholarship.mapper.SysSettingMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("SysSettingServiceImpl tests")
class SysSettingServiceImplTest {

    @Mock
    private SysSettingMapper sysSettingMapper;
    @Mock
    private Validator validator;

    private SysSettingServiceImpl sysSettingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sysSettingService = new SysSettingServiceImpl(sysSettingMapper, validator);
    }

    @Test
    void getBasicSettingParsesJson() {
        SysSetting setting = new SysSetting();
        setting.setSettingKey("basic");
        setting.setIsActive(1);
        setting.setSettingValue(JSON.toJSONString(createBasicSetting()));
        when(sysSettingMapper.selectList(any())).thenReturn(List.of(setting));

        BasicSetting result = sysSettingService.getBasicSetting();

        assertNotNull(result);
        assertEquals("system", result.getSystemName());
    }

    @Test
    void getAwardRulesReturnsRules() {
        SysSetting setting = new SysSetting();
        setting.setSettingKey("awards");
        setting.setIsActive(1);
        setting.setSettingValue(JSON.toJSONString(createAwardConfig()));
        when(sysSettingMapper.selectList(any())).thenReturn(List.of(setting));

        List<AwardRule> result = sysSettingService.getAwardRules();

        assertEquals(1, result.size());
        assertEquals("一等奖", result.get(0).getName());
    }

    @Test
    void updateWeightSettingValidatesThenUpdates() {
        WeightSetting weightSetting = new WeightSetting();
        weightSetting.setCourseWeight(40);
        weightSetting.setResearchWeight(30);
        weightSetting.setCompetitionWeight(10);
        weightSetting.setComprehensiveWeight(20);

        SysSetting existing = new SysSetting();
        existing.setId(1L);
        existing.setSettingKey("weight");
        existing.setIsActive(1);
        when(sysSettingMapper.selectList(any())).thenReturn(List.of(existing));
        when(sysSettingMapper.updateById(any(SysSetting.class))).thenReturn(1);
        when(validator.validate(any(WeightSetting.class))).thenReturn(Set.<ConstraintViolation<WeightSetting>>of());

        assertTrue(sysSettingService.updateSetting("weight", weightSetting));
    }

    @Test
    void getAllSettingsReturnsActiveMap() {
        SysSetting basic = new SysSetting();
        basic.setSettingKey("basic");
        basic.setSettingValue("{}");
        basic.setIsActive(1);
        when(sysSettingMapper.selectList(any())).thenReturn(List.of(basic));

        Map<String, String> result = sysSettingService.getAllSettings();

        assertEquals("{}", result.get("basic"));
    }

    private BasicSetting createBasicSetting() {
        BasicSetting basicSetting = new BasicSetting();
        basicSetting.setSystemName("system");
        basicSetting.setSystemShortName("sys");
        return basicSetting;
    }

    private AwardConfig createAwardConfig() {
        AwardRule rule = new AwardRule();
        rule.setId("1");
        rule.setName("一等奖");
        rule.setRatio(10);
        rule.setAmount(1000);
        AwardConfig config = new AwardConfig();
        config.setRules(List.of(rule));
        return config;
    }
}
