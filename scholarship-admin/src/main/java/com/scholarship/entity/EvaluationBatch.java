package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.DecimalMin;

/**
 * 评定批次实体类
 * <p>
 * 对应数据库表：evaluation_batch
 * 存储奖学金评定批次信息，如2024年秋季学期奖学金评定
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("evaluation_batch")
@Schema(description = "评定批次")
public class EvaluationBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "批次ID")
    private Long id;

    /**
     * 批次名称
     */
    @Schema(description = "批次名称")
    @TableField("batch_name")
    @JsonProperty("name")
    private String batchName;

    /**
     * 批次编码
     */
    @Schema(description = "批次编码")
    private String batchCode;

    /**
     * 学年
     */
    @Schema(description = "学年")
    private String academicYear;

    /**
     * 学期
     * 1-第一学期 2-第二学期 3-全年
     */
    @Schema(description = "学期：1-第一学期 2-第二学期 3-全年")
    private Integer semester;

    /**
     * 申请开始日期
     */
    @Schema(description = "申请开始日期")
    @TableField("application_start_date")
    @JsonProperty("startDate")
    private LocalDate applicationStartDate;

    /**
     * 申请结束日期
     */
    @Schema(description = "申请结束日期")
    @TableField("application_end_date")
    @JsonProperty("endDate")
    private LocalDate applicationEndDate;

    /**
     * 评审开始日期
     */
    @Schema(description = "评审开始日期")
    private LocalDate reviewStartDate;

    /**
     * 评审结束日期
     */
    @Schema(description = "评审结束日期")
    private LocalDate reviewEndDate;

    /**
     * 公示开始日期
     */
    @Schema(description = "公示开始日期")
    private LocalDate publicityStartDate;

    /**
     * 公示结束日期
     */
    @Schema(description = "公示结束日期")
    private LocalDate publicityEndDate;

    /**
     * 批次状态
     * 1-未开始 2-申请中 3-评审中 4-公示中 5-已完成
     */
    @Schema(description = "批次状态：1-未开始 2-申请中 3-评审中 4-公示中 5-已完成")
    @TableField("batch_status")
    @JsonProperty("status")
    private Integer batchStatus;

    /**
     * 奖学金总额
     */
    @Schema(description = "奖学金总额")
    @TableField("total_amount")
    @DecimalMin(value = "0", message = "奖学金总额不能为负数")
    private BigDecimal totalAmount;

    /**
     * 获奖人数
     */
    @Schema(description = "获奖人数")
    @TableField("winner_count")
    private Integer winnerCount;

    /**
     * 评定说明
     */
    @Schema(description = "评定说明")
    private String description;

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
    @TableField("create_time")
    @JsonProperty("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonProperty("updateTime")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
