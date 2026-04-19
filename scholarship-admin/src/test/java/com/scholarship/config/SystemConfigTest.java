package com.scholarship.config;

import com.scholarship.dto.AwardConfig;
import com.scholarship.dto.AwardRule;
import com.scholarship.dto.BasicSetting;
import com.scholarship.dto.WeightSetting;
import com.scholarship.service.SysSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SystemConfig 系统配置提供者测试
 */
@DisplayName("SystemConfig 系统配置提供者测试")
class SystemConfigTest {

    @Mock
    private SysSettingService settingService;

    private SystemConfig systemConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        systemConfig = new SystemConfig(settingService);
    }

    // ==================== 初始化测试 ====================

    @Test
    @DisplayName("测试初始化加载配置")
    void testInit() {
        // 准备模拟数据
        BasicSetting basic = createBasicSetting();
        WeightSetting weight = createWeightSetting();
        AwardConfig award = createAwardConfig();

        when(settingService.getBasicSetting()).thenReturn(basic);
        when(settingService.getWeightSetting()).thenReturn(weight);
        when(settingService.getAwardConfig()).thenReturn(award);

        // 执行初始化
        systemConfig.init();

        // 验证配置已加载
        assertEquals("研究生学业奖学金评定系统", systemConfig.getSystemName());
        assertEquals(40, systemConfig.getCourseWeight());
        assertNotNull(systemConfig.getAwardConfig());

        verify(settingService).getBasicSetting();
        verify(settingService).getWeightSetting();
        verify(settingService).getAwardConfig();
    }

    @Test
    @DisplayName("测试初始化 - 配置为空时使用默认值")
    void testInit_WithNullSettings() {
        when(settingService.getBasicSetting()).thenReturn(null);
        when(settingService.getWeightSetting()).thenReturn(null);
        when(settingService.getAwardConfig()).thenReturn(null);

        systemConfig.init();

        // 基本设置为 null
        assertNull(systemConfig.getSystemName());
        assertNull(systemConfig.getSystemShortName());

        // 权重设置使用默认值
        assertEquals(40, systemConfig.getCourseWeight());
        assertEquals(35, systemConfig.getResearchWeight());
        assertEquals(25, systemConfig.getComprehensiveWeight());

        // 奖项配置为 null
        assertNull(systemConfig.getAwardConfig());
        assertTrue(systemConfig.getAwardRules().isEmpty());
    }

    // ==================== 基本设置测试 ====================

    @Test
    @DisplayName("测试获取系统名称")
    void testGetSystemName() {
        BasicSetting basic = createBasicSetting();
        when(settingService.getBasicSetting()).thenReturn(basic);

        systemConfig.refresh();

        assertEquals("研究生学业奖学金评定系统", systemConfig.getSystemName());
    }

    @Test
    @DisplayName("测试获取系统简称")
    void testGetSystemShortName() {
        BasicSetting basic = createBasicSetting();
        when(settingService.getBasicSetting()).thenReturn(basic);

        systemConfig.refresh();

        assertEquals("奖学金评定系统", systemConfig.getSystemShortName());
    }

    @Test
    @DisplayName("测试获取当前学期")
    void testGetCurrentSemester() {
        BasicSetting basic = createBasicSetting();
        when(settingService.getBasicSetting()).thenReturn(basic);

        systemConfig.refresh();

        assertEquals("2025-1", systemConfig.getCurrentSemester());
    }

    @Test
    @DisplayName("测试获取管理员邮箱")
    void testGetAdminEmail() {
        BasicSetting basic = createBasicSetting();
        when(settingService.getBasicSetting()).thenReturn(basic);

        systemConfig.refresh();

        assertEquals("admin@example.com", systemConfig.getAdminEmail());
    }

    @Test
    @DisplayName("测试获取联系电话")
    void testGetAdminPhone() {
        BasicSetting basic = createBasicSetting();
        when(settingService.getBasicSetting()).thenReturn(basic);

        systemConfig.refresh();

        assertEquals("010-12345678", systemConfig.getAdminPhone());
    }

    @Test
    @DisplayName("测试获取系统公告")
    void testGetAnnouncement() {
        BasicSetting basic = createBasicSetting();
        basic.setAnnouncement("这是一条测试公告");
        when(settingService.getBasicSetting()).thenReturn(basic);

        systemConfig.refresh();

        assertEquals("这是一条测试公告", systemConfig.getAnnouncement());
    }

    @Test
    @DisplayName("测试基本设置 - 数据为空返回 null")
    void testBasicSetting_Null() {
        when(settingService.getBasicSetting()).thenReturn(null);

        systemConfig.refresh();

        assertNull(systemConfig.getSystemName());
        assertNull(systemConfig.getSystemShortName());
        assertNull(systemConfig.getCurrentSemester());
        assertNull(systemConfig.getAdminEmail());
        assertNull(systemConfig.getAdminPhone());
        assertNull(systemConfig.getAnnouncement());
    }

    // ==================== 权重设置测试 ====================

    @Test
    @DisplayName("测试获取课程成绩权重")
    void testGetCourseWeight() {
        WeightSetting weight = createWeightSetting();
        when(settingService.getWeightSetting()).thenReturn(weight);

        systemConfig.refresh();

        assertEquals(40, systemConfig.getCourseWeight());
    }

    @Test
    @DisplayName("测试获取科研成果权重")
    void testGetResearchWeight() {
        WeightSetting weight = createWeightSetting();
        when(settingService.getWeightSetting()).thenReturn(weight);

        systemConfig.refresh();

        assertEquals(35, systemConfig.getResearchWeight());
    }

    @Test
    @DisplayName("测试获取综合素质权重")
    void testGetComprehensiveWeight() {
        WeightSetting weight = createWeightSetting();
        when(settingService.getWeightSetting()).thenReturn(weight);

        systemConfig.refresh();

        assertEquals(25, systemConfig.getComprehensiveWeight());
    }

    @Test
    @DisplayName("测试权重设置 - 数据为空使用默认值")
    void testWeightSetting_Null() {
        when(settingService.getWeightSetting()).thenReturn(null);

        systemConfig.refresh();

        assertEquals(40, systemConfig.getCourseWeight());
        assertEquals(35, systemConfig.getResearchWeight());
        assertEquals(25, systemConfig.getComprehensiveWeight());
    }

    @Test
    @DisplayName("测试权重设置 - 部分字段为0")
    void testWeightSetting_ZeroValues() {
        WeightSetting weight = new WeightSetting();
        weight.setCourseWeight(0);
        weight.setResearchWeight(100);
        weight.setComprehensiveWeight(0);

        when(settingService.getWeightSetting()).thenReturn(weight);

        systemConfig.refresh();

        assertEquals(0, systemConfig.getCourseWeight());
        assertEquals(100, systemConfig.getResearchWeight());
        assertEquals(0, systemConfig.getComprehensiveWeight());
    }

    // ==================== 奖项配置测试 ====================

    @Test
    @DisplayName("测试获取奖项配置")
    void testGetAwardConfig() {
        AwardConfig award = createAwardConfig();
        when(settingService.getAwardConfig()).thenReturn(award);

        systemConfig.refresh();

        assertNotNull(systemConfig.getAwardConfig());
        assertEquals("1.0", systemConfig.getAwardConfig().getVersion());
        assertEquals("2025年奖项配置", systemConfig.getAwardConfig().getName());
    }

    @Test
    @DisplayName("测试获取奖项规则列表")
    void testGetAwardRules() {
        AwardConfig award = createAwardConfig();
        when(settingService.getAwardConfig()).thenReturn(award);

        systemConfig.refresh();

        List<AwardRule> rules = systemConfig.getAwardRules();
        assertNotNull(rules);
        assertEquals(3, rules.size());
        assertEquals("一等奖", rules.get(0).getName());
        assertEquals("二等奖", rules.get(1).getName());
        assertEquals("三等奖", rules.get(2).getName());
    }

    @Test
    @DisplayName("测试获取奖项规则 - 配置为空返回空列表")
    void testGetAwardRules_NullConfig() {
        when(settingService.getAwardConfig()).thenReturn(null);

        systemConfig.refresh();

        List<AwardRule> rules = systemConfig.getAwardRules();
        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    @DisplayName("测试获取奖项规则 - 规则列表为空")
    void testGetAwardRules_EmptyRules() {
        AwardConfig award = new AwardConfig();
        award.setVersion("1.0");
        award.setRules(null);

        when(settingService.getAwardConfig()).thenReturn(award);

        systemConfig.refresh();

        List<AwardRule> rules = systemConfig.getAwardRules();
        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    @DisplayName("测试获取分配策略")
    void testGetAllocationStrategy() {
        AwardConfig award = createAwardConfig();
        when(settingService.getAwardConfig()).thenReturn(award);

        systemConfig.refresh();

        assertEquals("scorePriority", systemConfig.getAllocationStrategy());
    }

    @Test
    @DisplayName("测试获取分配策略 - 配置为空使用默认值")
    void testGetAllocationStrategy_Null() {
        when(settingService.getAwardConfig()).thenReturn(null);

        systemConfig.refresh();

        assertEquals("scorePriority", systemConfig.getAllocationStrategy());
    }

    // ==================== 刷新功能测试 ====================

    @Test
    @DisplayName("测试刷新配置")
    void testRefresh() {
        // 初始配置
        BasicSetting basic1 = createBasicSetting();
        basic1.setSystemName("旧系统名称");
        when(settingService.getBasicSetting()).thenReturn(basic1);
        systemConfig.refresh();
        assertEquals("旧系统名称", systemConfig.getSystemName());

        // 更新配置
        BasicSetting basic2 = createBasicSetting();
        basic2.setSystemName("新系统名称");
        when(settingService.getBasicSetting()).thenReturn(basic2);
        systemConfig.refresh();
        assertEquals("新系统名称", systemConfig.getSystemName());
    }

    @Test
    @DisplayName("测试刷新 - 清除旧缓存")
    void testRefresh_ClearsOldCache() {
        // 第一次加载
        WeightSetting weight1 = createWeightSetting();
        weight1.setCourseWeight(30);
        when(settingService.getWeightSetting()).thenReturn(weight1);
        systemConfig.refresh();
        assertEquals(30, systemConfig.getCourseWeight());

        // 刷新后权重改变
        WeightSetting weight2 = createWeightSetting();
        weight2.setCourseWeight(50);
        when(settingService.getWeightSetting()).thenReturn(weight2);
        systemConfig.refresh();
        assertEquals(50, systemConfig.getCourseWeight());
    }

    // ==================== 辅助方法 ====================

    private BasicSetting createBasicSetting() {
        BasicSetting setting = new BasicSetting();
        setting.setSystemName("研究生学业奖学金评定系统");
        setting.setSystemShortName("奖学金评定系统");
        setting.setCurrentSemester("2025-1");
        setting.setAdminEmail("admin@example.com");
        setting.setAdminPhone("010-12345678");
        setting.setAnnouncement("");
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
        rule1.setPriority(1);

        AwardRule rule2 = new AwardRule();
        rule2.setId("award_2");
        rule2.setName("二等奖");
        rule2.setRatio(20);
        rule2.setAmount(8000);
        rule2.setPriority(2);

        AwardRule rule3 = new AwardRule();
        rule3.setId("award_3");
        rule3.setName("三等奖");
        rule3.setRatio(30);
        rule3.setAmount(5000);
        rule3.setPriority(3);

        config.setRules(List.of(rule1, rule2, rule3));
        return config;
    }
}
