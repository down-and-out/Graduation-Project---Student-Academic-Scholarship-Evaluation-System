package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评定结果实体类
 * <p>
 * 对应数据库表：evaluation_result
 * 存储奖学金评定的最终结果
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Data
@TableName("evaluation_result")
@Schema(description = "评定结果")
public class EvaluationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 结果 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "结果 ID")
    private Long id;

    /**
     * 批次 ID
     */
    @Schema(description = "批次 ID")
    private Long batchId;

    /**
     * 申请 ID
     */
    @Schema(description = "申请 ID")
    private Long applicationId;

    /**
     * 学生 ID
     */
    @Schema(description = "学生 ID")
    private Long studentId;

    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名")
    private String studentName;

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentNo;

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
     * 课程成绩分数
     */
    @Schema(description = "课程成绩分数")
    private BigDecimal courseScore;

    /**
     * 科研成果分数
     */
    @Schema(description = "科研成果分数")
    private BigDecimal researchScore;

    /**
     * 竞赛获奖分数
     */
    @Schema(description = "竞赛获奖分数")
    private BigDecimal competitionScore;

    /**
     * 综合素质分数
     */
    @Schema(description = "综合素质分数")
    private BigDecimal qualityScore;

    /**
     * 总分
     */
    @Schema(description = "总分")
    private BigDecimal totalScore;

    /**
     * 院系排名
     */
    @Schema(description = "院系排名")
    private Integer departmentRank;

    /**
     * 专业排名
     */
    @Schema(description = "专业排名")
    private Integer majorRank;

    /**
     * 获奖等级
     * 1-特等学金 2-一等奖学金 3-二等奖学金 4-三等奖学金 5-未获奖
     */
    @Schema(description = "获奖等级：1-特等 2-一等 3-二等 4-三等 5-未获奖")
    private Integer awardLevel;

    /**
     * 奖学金金额
     */
    @Schema(description = "奖学金金额")
    private BigDecimal awardAmount;

    /**
     * 结果状态
     * 1-公示中 2-已确定 3-有异议
     */
    @Schema(description = "结果状态：1-公示中 2-已确定 3-有异议")
    private Integer resultStatus;

    /**
     * 公示日期
     */
    @Schema(description = "公示日期")
    private LocalDateTime publicityDate;

    /**
     * 确定日期
     */
    @Schema(description = "确定日期")
    private LocalDateTime confirmDate;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "乐观锁版本号")
    private Integer version;

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

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
