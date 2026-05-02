package com.scholarship.controller;

import com.alibaba.fastjson2.JSON;
import com.scholarship.common.result.Result;
import com.scholarship.config.SystemConfig;
import com.scholarship.dto.AwardConfig;
import com.scholarship.dto.BasicSetting;
import com.scholarship.dto.WeightSetting;
import com.scholarship.entity.SysSetting;
import com.scholarship.service.SysSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统设置控制器
 *
 * 功能说明：
 * - 获取所有系统设置（key-value 形式）
 * - 获取指定设置（支持类型化返回）
 * - 更新系统设置（仅管理员）
 *
 * 设置类型：
 * - basic：基本设置（系统名称、学期、公告等）
 * - weight：评分权重（课程成绩、科研成果、综合素质权重）
 * - awards：奖项配置
 *
 * 缓存说明：
 * - 使用 Spring Cache 缓存设置值
 * - 更新后自动清除缓存
 */
@Slf4j
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@Tag(name = "系统设置", description = "系统配置管理接口")
public class SystemSettingController {

    private final SysSettingService sysSettingService;
    private final SystemConfig systemConfig;

    /**
     * 获取所有系统设置
     * 返回当前生效的所有设置（key-value 形式）
     *
     * @return 所有设置 Map
     */
    @GetMapping("/settings")
    @Operation(summary = "获取所有设置", description = "获取当前生效的系统设置 key-value 映射")
    public Result<Map<String, String>> getAllSettings() {
        return Result.success(sysSettingService.getAllSettings());
    }

    /**
     * 获取指定设置
     * 根据 key 获取结构化设置值，支持类型化返回
     *
     * @param key    设置键（如：basic、weight、awards）
     * @param active 是否获取当前生效版本（true=从active表查询）
     * @return 设置值
     */
    @GetMapping("/setting/{key}")
    @Operation(summary = "获取设置", description = "根据 key 获取结构化设置值")
    public Result<Object> getSetting(
            @Parameter(description = "设置键", example = "basic")
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "false") boolean active) {
        return Result.success("操作成功", getTypedSetting(key, active));
    }

    /**
     * 更新系统设置（仅管理员）
     * 更新指定 key 的设置值，更新后自动清除缓存
     *
     * @param key  设置键
     * @param data 设置值（对象）
     * @return 操作结果
     */
    @PutMapping("/setting/{key}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新设置", description = "更新指定 key 的设置值")
    public Result<Void> updateSetting(
            @Parameter(description = "设置键", example = "basic")
            @PathVariable String key,
            @RequestBody Object data) {

        log.info("管理员更新系统设置，key={}", key);

        boolean success = sysSettingService.updateSetting(key, data);
        if (success) {
            // 刷新系统配置缓存
            systemConfig.refresh();
            log.info("系统设置更新成功，key={}", key);
            return Result.success("保存成功");
        }

        log.error("系统设置更新失败，key={}", key);
        return Result.error("保存失败");
    }

    /**
     * 根据 key 获取类型化设置值
     * 支持 basic、weight、awards 三种预设类型
     * 其他类型返回原始 JSON 字符串
     */
    private Object getTypedSetting(String key, boolean active) {
        return switch (key) {
            case "basic" -> active
                    ? sysSettingService.getActiveSetting(key, BasicSetting.class)
                    : sysSettingService.getBasicSetting();
            case "weight" -> active
                    ? sysSettingService.getActiveSetting(key, WeightSetting.class)
                    : sysSettingService.getWeightSetting();
            case "awards" -> active
                    ? sysSettingService.getActiveSetting(key, AwardConfig.class)
                    : sysSettingService.getAwardConfig();
            default -> parseUnknownSetting(key);
        };
    }

    /**
     * 解析未知类型的设置
     * 尝试解析为 JSON 对象，失败则返回原始字符串
     */
    private Object parseUnknownSetting(String key) {
        SysSetting setting = sysSettingService.getByKey(key);
        if (setting == null || setting.getSettingValue() == null) {
            return null;
        }

        try {
            return JSON.parse(setting.getSettingValue());
        } catch (Exception e) {
            log.warn("解析系统设置失败，回退原始字符串，key={}", key, e);
            return setting.getSettingValue();
        }
    }
}
