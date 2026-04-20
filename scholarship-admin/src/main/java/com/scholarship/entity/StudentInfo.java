package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 研究生信息实体类
 * <p>
 * 对应数据库表：student_info
 * 存储研究生的详细学籍信息
 * </p>
 *
 * 数据库索引建议：
 * <pre>
 * CREATE INDEX idx_user_id ON student_info(user_id);
 * CREATE INDEX idx_tutor_id ON student_info(tutor_id);
 * CREATE INDEX idx_status ON student_info(status);
 * CREATE INDEX idx_student_no ON student_info(student_no);
 * </pre>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("student_info")
@Schema(description = "研究生信息")
public class StudentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    /**
     * 关联用户 ID
     */
    @NotNull(message = "关联用户 ID 不能为空")
    @Schema(description = "关联用户 ID")
    private Long userId;

    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号")
    private String studentNo;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名")
    private String name;

    /**
     * 性别：0-女 1-男
     */
    @NotNull(message = "性别不能为空")
    @Schema(description = "性别：0-女 1-男")
    private Integer gender;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
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
    @Schema(description = "学历层次：1-硕士 2-博士")
    private Integer educationLevel;

    /**
     * 培养方式：1-全日制 2-非全日制
     */
    @NotNull(message = "培养方式不能为空")
    @Schema(description = "培养方式：1-全日制 2-非全日制")
    private Integer trainingMode;

    /**
     * 院系
     */
    @Schema(description = "院系")
    private String department;

    /**
     * 专业
     */
    @Schema(description = "专业")
    private String major;

    /**
     * 班级
     */
    @Schema(description = "班级")
    private String className;

    /**
     * 导师 ID（关联导师用户表）
     */
    @Schema(description = "导师 ID")
    private Long tutorId;

    @TableField(exist = false)
    @Schema(description = "瀵煎笀濮撳悕")
    private String tutorName;

    /**
     * 研究方向
     */
    @Schema(description = "研究方向")
    private String direction;

    /**
     * 政治面貌
     */
    @Schema(description = "政治面貌")
    private String politicalStatus;

    /**
     * 民族
     */
    @Schema(description = "民族")
    private String nation;

    /**
     * 籍贯
     */
    @NotBlank(message = "籍贯不能为空")
    @Schema(description = "籍贯")
    private String nativePlace;

    /**
     * 家庭住址
     */
    @NotBlank(message = "家庭住址不能为空")
    @Schema(description = "家庭住址")
    private String address;

    /**
     * 电话
     */
    @Schema(description = "电话")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 学籍状态：0-休学 1-在读 2-毕业 3-退学
     */
    @NotNull(message = "学籍状态不能为空")
    @Schema(description = "学籍状态：0-休学 1-在读 2-毕业 3-退学")
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
