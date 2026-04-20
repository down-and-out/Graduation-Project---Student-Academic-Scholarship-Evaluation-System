package com.scholarship.dto;

import com.scholarship.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

/**
 * 用户创建请求 DTO
 * 包含用户信息及学生档案额外字段
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@Schema(description = "用户创建请求")
public class UserCreateRequest {

    /**
     * 用户信息
     */
    @Valid
    @Schema(description = "用户信息")
    private SysUser user;

    /**
     * 专业（仅学生类型需要）
     */
    @Schema(description = "专业（仅学生类型需要）")
    private String major;

    // ==================== 学生信息扩展字段 ====================

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentNo;

    /**
     * 性别：0-女 1-男
     */
    @Schema(description = "性别：0-女 1-男")
    private Integer gender;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCard;

    /**
     * 入学年份
     */
    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    /**
     * 学历层次：1-硕士 2-博士
     */
    @Schema(description = "学历层次：1-硕士 2-博士")
    private Integer educationLevel;

    /**
     * 培养方式：1-全日制 2-非全日制
     */
    @Schema(description = "培养方式：1-全日制 2-非全日制")
    private Integer trainingMode;

    /**
     * 籍贯
     */
    @Schema(description = "籍贯")
    private String nativePlace;

    /**
     * 家庭住址
     */
    @Schema(description = "家庭住址")
    private String address;

    /**
     * 学籍状态：0-休学 1-在读 2-毕业 3-退学
     */
    @Schema(description = "学籍状态：0-休学 1-在读 2-毕业 3-退学")
    private Integer status;
}
