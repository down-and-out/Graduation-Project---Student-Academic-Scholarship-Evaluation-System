package com.scholarship.config;

import com.scholarship.dto.*;
import com.scholarship.service.SysSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 系统配置提供者
 * <p>
 * 为业务层提供便捷的设置访问方式
 * 启动时加载配置到内存，提高访问性能
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConfig {

    private final SysSettingService settingService;

    // 缓存的配置
    private BasicSetting basicSetting;
    private WeightSetting weightSetting;
    private AwardConfig awardConfig;

    /**
     * 初始化，加载配置到内存
     */
    @PostConstruct
    public void init() {
        log.info("加载系统配置到内存...");
        refresh();
        log.info("系统配置加载完成");
    }

    /**
     * 刷新内存中的配置
     */
    public void refresh() {
        this.basicSetting = settingService.getBasicSetting();
        this.weightSetting = settingService.getWeightSetting();
        this.awardConfig = settingService.getAwardConfig();

        if (this.basicSetting == null) {
            log.warn("基本设置未配置，使用默认值");
            this.basicSetting = new BasicSetting();
        }
        if (this.weightSetting == null) {
            log.warn("权重设置未配置，使用默认值");
            this.weightSetting = new WeightSetting();
            this.weightSetting.setCourseWeight(40);
            this.weightSetting.setResearchWeight(35);
            this.weightSetting.setComprehensiveWeight(25);
        }
        if (this.awardConfig == null) {
            log.warn("奖项配置未配置");
        }
    }

    // ==================== 基本设置 ====================

    public String getSystemName() {
        return basicSetting != null ? basicSetting.getSystemName() : null;
    }

    public String getSystemShortName() {
        return basicSetting != null ? basicSetting.getSystemShortName() : null;
    }

    public String getCurrentSemester() {
        return basicSetting != null ? basicSetting.getCurrentSemester() : null;
    }

    public String getAdminEmail() {
        return basicSetting != null ? basicSetting.getAdminEmail() : null;
    }

    public String getAdminPhone() {
        return basicSetting != null ? basicSetting.getAdminPhone() : null;
    }

    public String getAnnouncement() {
        return basicSetting != null ? basicSetting.getAnnouncement() : null;
    }

    // ==================== 权重设置 ====================

    public int getCourseWeight() {
        return weightSetting != null ? weightSetting.getCourseWeight() : 40;
    }

    public int getResearchWeight() {
        return weightSetting != null ? weightSetting.getResearchWeight() : 35;
    }

    public int getComprehensiveWeight() {
        return weightSetting != null ? weightSetting.getComprehensiveWeight() : 25;
    }

    // ==================== 奖项配置 ====================

    public AwardConfig getAwardConfig() {
        return awardConfig;
    }

    public List<AwardRule> getAwardRules() {
        if (awardConfig == null || awardConfig.getRules() == null) {
            return List.of();
        }
        return awardConfig.getRules();
    }

    public String getAllocationStrategy() {
        if (awardConfig == null) {
            return "scorePriority";
        }
        return awardConfig.getAllocationStrategy();
    }
}
