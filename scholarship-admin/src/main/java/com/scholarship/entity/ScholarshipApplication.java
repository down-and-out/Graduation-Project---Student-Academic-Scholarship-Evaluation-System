package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 奖学金申请实体类
 * <p>
 * 对应数据库表：scholarship_application
 * 存储学生的奖学金申请记录
 * </p>
 *
 * 数据库索引建议：
 * <pre>
 * CREATE INDEX idx_batch_id ON scholarship_application(batch_id);
 * CREATE INDEX idx_student_id ON scholarship_application(student_id);
 * CREATE INDEX idx_status ON scholarship_application(status);
 * </pre>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("scholarship_application")
@Schema(description = "奖学金申请")
public class ScholarshipApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    /**
     * 评定批次 ID
     */
    @NotNull(message = "评定批次 ID 不能为空")
    @Schema(description = "评定批次 ID")
    private Long batchId;

    /**
     * 学生 ID
     */
    @NotNull(message = "学生 ID 不能为空")
    @Schema(description = "学生 ID")
    private Long studentId;

    /**
     * 申请编号
     */
    @Schema(description = "申请编号")
    private String applicationNo;

    /**
     * 总评分
     */
    @Schema(description = "总评分")
    private BigDecimal totalScore;

    /**
     * 排名
     */
    @Schema(description = "排名")
    private Integer ranking;

    /**
     * 获奖等级：1-一等奖学金 2-二等奖学金 3-三等奖学金 4-未获奖
     */
    @Schema(description = "获奖等级")
    private Integer awardLevel;

    /**
     * 奖学金金额
     */
    @Schema(description = "奖学金金额")
    private BigDecimal scholarshipAmount;

    /**
     * 申请时间
     */
    @Schema(description = "申请时间")
    private LocalDateTime applicationTime;

    /**
     * 提交时间
     */
    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    /**
     * 状态：0-草稿 1-已提交 2-审核中 3-评审完成 4-已公示 5-已发放 6-已拒绝
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态")
    private Integer status;

    /**
     * 导师意见
     */
    @Schema(description = "导师意见")
    private String tutorOpinion;

    /**
     * 导师审核人 ID
     */
    @Schema(description = "导师审核人 ID")
    private Long tutorId;

    /**
     * 导师审核时间
     */
    @Schema(description = "导师审核时间")
    private LocalDateTime tutorReviewTime;

    /**
     * 院系意见
     */
    @Schema(description = "院系意见")
    private String collegeOpinion;

    /**
     * 院系审核人 ID
     */
    @Schema(description = "院系审核人 ID")
    private Long collegeReviewerId;

    /**
     * 院系审核时间
     */
    @Schema(description = "院系审核时间")
    private LocalDateTime collegeReviewTime;

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
    @Schema(description = "逻辑删除")
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
