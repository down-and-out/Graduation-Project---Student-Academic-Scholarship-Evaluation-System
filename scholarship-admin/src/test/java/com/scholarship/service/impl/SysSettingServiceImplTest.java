package com.scholarship.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.config.SystemConfig;
import com.scholarship.dto.AwardConfig;
import com.scholarship.dto.AwardRule;
import com.scholarship.dto.BasicSetting;
import com.scholarship.dto.WeightSetting;
import com.scholarship.entity.SysSetting;
import com.scholarship.mapper.SysSettingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysSettingServiceImpl 系统设置服务测试
 */
@DisplayName("SysSettingServiceImpl 系统设置服务测试")
class SysSettingServiceImplTest {

    @Mock
    private SysSettingMapper sysSettingMapper;

    @Mock
    private SystemConfig systemConfig;

    private SysSettingServiceImpl sysSettingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sysSettingService = new SysSettingServiceImpl(sysSettingMapper, systemConfig);
    }

    // ==================== 基本设置测试 ====================

    @Test
    @DisplayName("测试获取基本设置 - 正常情况")
    void testGetBasicSetting_Success() {
        // 准备测试数据
        BasicSetting basicSetting = createBasicSetting();
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSettingKey("basic");
        sysSetting.setSettingValue(JSON.toJSONString(basicSetting));

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(sysSetting);

        // 执行测试
        BasicSetting result = sysSettingService.getBasicSetting();

        // 验证结果
        assertNotNull(result);
        assertEquals("研究生学业奖学金评定系统", result.getSystemName());
        assertEquals("奖学金评定系统", result.getSystemShortName());
        assertEquals("2025-1", result.getCurrentSemester());
        assertEquals("admin@example.com", result.getAdminEmail());
    }

    @Test
    @DisplayName("测试获取基本设置 - 数据不存在时返回 null")
    void testGetBasicSetting_NotFound() {
        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);

        BasicSetting result = sysSettingService.getBasicSetting();

        assertNull(result);
    }

    @Test
    @DisplayName("测试更新基本设置 - 更新成功并刷新缓存")
    void testUpdateBasicSetting_Success() {
        // 准备测试数据
        BasicSetting basicSetting = createBasicSetting();
        SysSetting existingSetting = new SysSetting();
        existingSetting.setId(1L);
        existingSetting.setSettingKey("basic");
        existingSetting.setSettingValue("{}");

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(existingSetting);
        when(sysSettingMapper.updateById(any(SysSetting.class)))
                .thenReturn(1);
        doNothing().when(systemConfig).refresh();

        // 执行测试
        boolean result = sysSettingService.updateSetting("basic", basicSetting);

        // 验证结果
        assertTrue(result);
        verify(sysSettingMapper, times(1)).updateById(any(SysSetting.class));
        verify(systemConfig, times(1)).refresh();
    }

    @Test
    @DisplayName("测试更新基本设置 - 新增设置")
    void testUpdateBasicSetting_Insert() {
        BasicSetting basicSetting = createBasicSetting();

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);
        when(sysSettingMapper.insert(any(SysSetting.class)))
                .thenReturn(1);
        doNothing().when(systemConfig).refresh();

        boolean result = sysSettingService.updateSetting("basic", basicSetting);

        assertTrue(result);
        verify(sysSettingMapper, times(1)).insert(any(SysSetting.class));
        verify(sysSettingMapper, never()).updateById(any(SysSetting.class));
        verify(systemConfig, times(1)).refresh();
    }

    // ==================== 权重设置测试 ====================

    @Test
    @DisplayName("测试获取权重设置 - 正常情况")
    void testGetWeightSetting_Success() {
        WeightSetting weightSetting = createWeightSetting();
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSettingKey("weight");
        sysSetting.setSettingValue(JSON.toJSONString(weightSetting));

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(sysSetting);

        WeightSetting result = sysSettingService.getWeightSetting();

        assertNotNull(result);
        assertEquals(40, result.getCourseWeight());
        assertEquals(35, result.getResearchWeight());
        assertEquals(25, result.getComprehensiveWeight());
    }

    @Test
    @DisplayName("测试更新权重设置 - 权重总和等于100")
    void testUpdateWeightSetting_Total100() {
        WeightSetting weightSetting = createWeightSetting();
        SysSetting existingSetting = new SysSetting();
        existingSetting.setId(2L);
        existingSetting.setSettingKey("weight");

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(existingSetting);
        when(sysSettingMapper.updateById(any(SysSetting.class)))
                .thenReturn(1);
        doNothing().when(systemConfig).refresh();

        boolean result = sysSettingService.updateSetting("weight", weightSetting);

        assertTrue(result);
        verify(systemConfig).refresh();
    }

    // ==================== 奖项配置测试 ====================

    @Test
    @DisplayName("测试获取奖项配置 - 正常情况")
    void testGetAwardConfig_Success() {
        AwardConfig awardConfig = createAwardConfig();
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSettingKey("awards");
        sysSetting.setSettingValue(JSON.toJSONString(awardConfig));

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(sysSetting);

        AwardConfig result = sysSettingService.getAwardConfig();

        assertNotNull(result);
        assertEquals("1.0", result.getVersion());
        assertEquals("2025年奖项配置", result.getName());
        assertEquals(3, result.getRules().size());
        assertEquals("scorePriority", result.getAllocationStrategy());
    }

    @Test
    @DisplayName("测试获取奖项规则列表")
    void testGetAwardRules() {
        AwardConfig awardConfig = createAwardConfig();
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSettingKey("awards");
        sysSetting.setSettingValue(JSON.toJSONString(awardConfig));

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(sysSetting);

        List<AwardRule> result = sysSettingService.getAwardRules();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("一等奖", result.get(0).getName());
        assertEquals(10, result.get(0).getRatio());
        assertEquals(10000, result.get(0).getAmount());
    }

    @Test
    @DisplayName("测试获取奖项规则 - 配置为空时返回空列表")
    void testGetAwardRules_EmptyConfig() {
        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);

        List<AwardRule> result = sysSettingService.getAwardRules();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试更新奖项配置 - 包含完整规则信息")
    void testUpdateAwardConfig_Success() {
        AwardConfig awardConfig = createAwardConfig();
        SysSetting existingSetting = new SysSetting();
        existingSetting.setId(3L);
        existingSetting.setSettingKey("awards");

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(existingSetting);
        when(sysSettingMapper.updateById(any(SysSetting.class)))
                .thenReturn(1);
        doNothing().when(systemConfig).refresh();

        boolean result = sysSettingService.updateSetting("awards", awardConfig);

        assertTrue(result);

        // 验证保存的 JSON 包含所有字段
        verify(sysSettingMapper).updateById(argThat((SysSetting setting) -> {
            String json = setting.getSettingValue();
            AwardConfig saved = JSON.parseObject(json, AwardConfig.class);
            return saved.getRules().size() == 3 &&
                   saved.getRules().get(0).getScoreRange() != null &&
                   saved.getRules().get(0).getPriority() != null;
        }));
    }

    // ==================== 通用方法测试 ====================

    @Test
    @DisplayName("测试获取所有设置")
    void testGetAllSettings() {
        SysSetting basic = new SysSetting();
        basic.setSettingKey("basic");
        basic.setSettingValue("{}");

        SysSetting weight = new SysSetting();
        weight.setSettingKey("weight");
        weight.setSettingValue("{}");

        when(sysSettingMapper.selectList(null))
                .thenReturn(List.of(basic, weight));

        Map<String, String> result = sysSettingService.getAllSettings();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("basic"));
        assertTrue(result.containsKey("weight"));
    }

    @Test
    @DisplayName("测试根据 key 获取设置")
    void testGetByKey() {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSettingKey("basic");
        sysSetting.setSettingValue("{}");

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(sysSetting);

        SysSetting result = sysSettingService.getByKey("basic");

        assertNotNull(result);
        assertEquals("basic", result.getSettingKey());
    }

    @Test
    @DisplayName("测试获取设置 - 泛型方法")
    void testGetSetting_Generic() {
        BasicSetting basicSetting = createBasicSetting();
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSettingKey("basic");
        sysSetting.setSettingValue(JSON.toJSONString(basicSetting));

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(sysSetting);

        BasicSetting result = sysSettingService.getSetting("basic", BasicSetting.class);

        assertNotNull(result);
        assertEquals(basicSetting.getSystemName(), result.getSystemName());
    }

    @Test
    @DisplayName("测试获取设置 - 设置不存在返回 null")
    void testGetSetting_NotFound() {
        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);

        BasicSetting result = sysSettingService.getSetting("nonexistent", BasicSetting.class);

        assertNull(result);
    }

    @Test
    @DisplayName("测试获取设置 - JSON 解析失败返回 null")
    void testGetSetting_InvalidJson() {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSettingKey("basic");
        sysSetting.setSettingValue("invalid json");

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(sysSetting);

        BasicSetting result = sysSettingService.getSetting("basic", BasicSetting.class);

        assertNull(result);
    }

    @Test
    @DisplayName("测试更新设置 - 更新失败不刷新缓存")
    void testUpdateSetting_Failure() {
        BasicSetting basicSetting = createBasicSetting();
        SysSetting existingSetting = new SysSetting();
        existingSetting.setId(1L);
        existingSetting.setSettingKey("basic");

        when(sysSettingMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(existingSetting);
        when(sysSettingMapper.updateById(any(SysSetting.class)))
                .thenReturn(0);

        boolean result = sysSettingService.updateSetting("basic", basicSetting);

        assertFalse(result);
        verify(systemConfig, never()).refresh();
    }

    // ==================== 辅助方法 ====================

    private BasicSetting createBasicSetting() {
        BasicSetting setting = new BasicSetting();
        setting.setSystemName("研究生学业奖学金评定系统");
        setting.setSystemShortName("奖学金评定系统");
        setting.setCurrentSemester("2025-1");
        setting.setAdminEmail("admin@example.com");
        setting.setAdminPhone("010-12345678");
        setting.setAnnouncement("系统公告内容");
        return setting;
    }

    private WeightSetting createWeightSetting() {
        WeightSetting setting = new WeightSetting();
        setting.setCourseWeight(40);
        setting.setResearchWeight(35);
        setting.setComprehensiveWeight(25);
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
        rule1.setPriority(1);

        AwardRule rule2 = new AwardRule();
        rule2.setId("award_2");
        rule2.setName("二等奖");
        rule2.setRatio(20);
        rule2.setAmount(8000);
        rule2.setScoreRange(new com.scholarship.dto.ScoreRange() {{ setMin(80.0); setMax(89.99); }});
        rule2.setPriority(2);

        AwardRule rule3 = new AwardRule();
        rule3.setId("award_3");
        rule3.setName("三等奖");
        rule3.setRatio(30);
        rule3.setAmount(5000);
        rule3.setScoreRange(new com.scholarship.dto.ScoreRange() {{ setMin(70.0); setMax(79.99); }});
        rule3.setPriority(3);

        config.setRules(List.of(rule1, rule2, rule3));
        return config;
    }
}
