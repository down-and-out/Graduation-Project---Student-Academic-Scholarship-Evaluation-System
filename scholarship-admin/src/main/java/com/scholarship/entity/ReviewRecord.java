package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评审记录实体类
 * <p>
 * 对应数据库表：review_record
 * 存储奖学金申请的评审记录
 * </p>
 *
 * 数据库索引建议：
 * <pre>
 * CREATE INDEX idx_application_id ON review_record(application_id);
 * CREATE INDEX idx_reviewer_id ON review_record(reviewer_id);
 * </pre>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("review_record")
@Schema(description = "评审记录")
public class ReviewRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "记录 ID")
    private Long id;

    /**
     * 申请 ID
     */
    @NotNull(message = "申请 ID 不能为空")
    @Schema(description = "申请 ID")
    private Long applicationId;

    /**
     * 评审阶段
     * 1-导师审核 2-院系审核 3-学校审核
     */
    @NotNull(message = "评审阶段不能为空")
    @Schema(description = "评审阶段：1-导师审核 2-院系审核 3-学校审核")
    private Integer reviewStage;

    /**
     * 评审人 ID
     */
    @NotNull(message = "评审人 ID 不能为空")
    @Schema(description = "评审人 ID")
    private Long reviewerId;

    /**
     * 评审人姓名
     */
    @Schema(description = "评审人姓名")
    private String reviewerName;

    /**
     * 评审结果
     * 1-通过 2-驳回 3-待定
     */
    @NotNull(message = "评审结果不能为空")
    @Schema(description = "评审结果：1-通过 2-驳回 3-待定")
    private Integer reviewResult;

    /**
     * 评审分数
     */
    @Schema(description = "评审分数")
    private BigDecimal reviewScore;

    /**
     * 评审意见
     */
    @Schema(description = "评审意见")
    private String reviewComment;

    /**
     * 评审时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "评审时间")
    private LocalDateTime reviewTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;
}
