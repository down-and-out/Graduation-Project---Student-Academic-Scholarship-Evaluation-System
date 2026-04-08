package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 基本设置 DTO
 * 对应 sys_setting 表中 basic 的 JSON 结构
 */
@Data
@Schema(description = "基本设置")
public class BasicSetting {

    /**
     * 系统名称
     */
    @Schema(description = "系统名称")
    private String systemName;

    /**
     * 系统简称
     */
    @Schema(description = "系统简称")
    private String systemShortName;

    /**
     * 当前学期
     * 格式：YYYY-N（如：2025-1）
     */
    @Schema(description = "当前学期")
    private String currentSemester;

    /**
     * 管理员邮箱
     */
    @Schema(description = "管理员邮箱")
    private String adminEmail;

    /**
     * 管理员电话
     */
    @Schema(description = "管理员电话")
    private String adminPhone;

    /**
     * 系统公告
     */
    @Schema(description = "系统公告")
    private String announcement;
}
