package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程成绩实体类
 * <p>
 * 对应数据库表：course_score
 * 存储学生的课程成绩信息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("course_score")
@Schema(description = "课程成绩")
public class CourseScore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "记录 ID")
    private Long id;

    /**
     * 学生 ID
     */
    @NotNull(message = "学生不能为空")
    @Schema(description = "学生 ID")
    private Long studentId;

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentNo;

    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名")
    private String studentName;

    /**
     * 课程 ID
     */
    @Schema(description = "课程 ID")
    private Long courseId;

    /**
     * 课程名称
     */
    @NotBlank(message = "课程名称不能为空")
    @Schema(description = "课程名称")
    private String courseName;

    /**
     * 课程代码
     */
    @NotBlank(message = "课程代码不能为空")
    @Schema(description = "课程代码")
    private String courseCode;

    /**
     * 课程类型
     * 1-必修 2-选修 3-任选
     */
    @Schema(description = "课程类型：1-必修 2-选修 3-任选")
    private Integer courseType;

    /**
     * 学分
     */
    @NotNull(message = "学分不能为空")
    @Positive(message = "学分必须大于0")
    @Schema(description = "学分")
    private BigDecimal credit;

    /**
     * 成绩（数值型）。
     * <p>当成绩为"合格"/"通过"等文本型时，本字段为 {@code null}，文本存储在 {@link #scoreText} 中。</p>
     */
    @Schema(description = "成绩")
    private BigDecimal score;

    /**
     * 成绩文本
     */
    @TableField("score_text")
    @Schema(description = "成绩文本")
    private String scoreText;

    /**
     * 绩点
     */
    @Schema(description = "绩点")
    private BigDecimal gpa;

    /**
     * 学年
     */
    @NotBlank(message = "学年不能为空")
    @Schema(description = "学年")
    private String academicYear;

    /**
     * 学期
     * 1-第一学期 2-第二学期 3-夏季学期
     */
    @NotNull(message = "学期不能为空")
    @Schema(description = "学期：1-第一学期 2-第二学期 3-夏季学期")
    private Integer semester;

    /**
     * 考试日期
     */
    @Schema(description = "考试日期")
    private LocalDate examDate;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 逻辑删除标记
     * 0-未删除 1-已删除
     */
    @TableLogic
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
