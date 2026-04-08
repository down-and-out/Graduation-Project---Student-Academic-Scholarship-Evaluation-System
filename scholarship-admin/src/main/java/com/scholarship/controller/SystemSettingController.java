package com.scholarship.controller;

import com.scholarship.common.result.Result;
import com.scholarship.config.SystemConfig;
import com.scholarship.entity.SysSetting;
import com.scholarship.service.SysSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统设置控制器
 * <p>
 * 提供系统配置的查询和更新接口
 * </p>
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
     * 获取所有设置
     */
    @GetMapping("/settings")
    @Operation(summary = "获取所有设置", description = "获取系统所有设置的 key-value 映射")
    public Result<Map<String, String>> getAllSettings() {
        Map<String, String> settings = sysSettingService.getAllSettings();
        return Result.success(settings);
    }

    /**
     * 获取单个设置
     */
    @GetMapping("/setting/{key}")
    @Operation(summary = "获取设置", description = "根据 key 获取设置值（JSON 字符串）")
    public Result<String> getSetting(
            @Parameter(description = "设置键", example = "basic")
            @PathVariable String key) {
        SysSetting setting = sysSettingService.getByKey(key);
        String value = setting != null ? setting.getSettingValue() : null;
        // 使用 success(String message, T data) 确保 data 字段正确设置
        return Result.success("操作成功", value);
    }

    /**
     * 更新设置（管理员权限）
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
            // 刷新内存缓存
            systemConfig.refresh();
            log.info("系统设置更新成功，key={}", key);
            return Result.success("保存成功");
        } else {
            log.error("系统设置更新失败，key={}", key);
            return Result.error("保存失败");
        }
    }
}
