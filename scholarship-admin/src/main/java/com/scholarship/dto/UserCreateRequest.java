package com.scholarship.dto;

import com.scholarship.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "专业不能为空")
    @Schema(description = "专业（仅学生类型需要）")
    private String major;

    // ==================== 学生信息扩展字段 ====================

    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号")
    private String studentNo;

    /**
     * 性别：0-女 1-男
     */
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别值必须在0-1之间")
    @Max(value = 1, message = "性别值必须在0-1之间")
    @Schema(description = "性别：0-女 1-男")
    private Integer gender;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$",
             message = "身份证号格式不正确")
    @Schema(description = "身份证号")
    private String idCard;

    /**
     * 入学年份
     */
    @NotNull(message = "入学年份不能为空")
    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    /**
     * 学历层次：1-硕士 2-博士
     */
    @NotNull(message = "学历层次不能为空")
    @Min(value = 1, message = "学历层次值必须在1-2之间")
    @Max(value = 2, message = "学历层次值必须在1-2之间")
    @Schema(description = "学历层次：1-硕士 2-博士")
    private Integer educationLevel;

    /**
     * 培养方式：1-全日制 2-非全日制
     */
    @NotNull(message = "培养方式不能为空")
    @Min(value = 1, message = "培养方式值必须在1-2之间")
    @Max(value = 2, message = "培养方式值必须在1-2之间")
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
    @NotNull(message = "学籍状态不能为空")
    @Min(value = 0, message = "学籍状态值必须在0-3之间")
    @Max(value = 3, message = "学籍状态值必须在0-3之间")
    @Schema(description = "学籍状态：0-休学 1-在读 2-毕业 3-退学")
    private Integer status;
}
