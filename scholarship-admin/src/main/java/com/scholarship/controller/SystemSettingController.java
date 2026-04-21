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

@Slf4j
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@Tag(name = "系统设置", description = "系统配置管理接口")
public class SystemSettingController {

    private final SysSettingService sysSettingService;
    private final SystemConfig systemConfig;

    @GetMapping("/settings")
    @Operation(summary = "获取所有设置", description = "获取当前生效的系统设置 key-value 映射")
    public Result<Map<String, String>> getAllSettings() {
        return Result.success(sysSettingService.getAllSettings());
    }

    @GetMapping("/setting/{key}")
    @Operation(summary = "获取设置", description = "根据 key 获取结构化设置值")
    public Result<Object> getSetting(
            @Parameter(description = "设置键", example = "basic")
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "false") boolean active) {
        return Result.success("操作成功", getTypedSetting(key, active));
    }

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
            systemConfig.refresh();
            log.info("系统设置更新成功，key={}", key);
            return Result.success("保存成功");
        }

        log.error("系统设置更新失败，key={}", key);
        return Result.error("保存失败");
    }

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
