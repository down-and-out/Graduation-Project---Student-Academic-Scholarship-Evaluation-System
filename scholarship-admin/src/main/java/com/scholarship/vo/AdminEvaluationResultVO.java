package com.scholarship.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "管理员端评定结果视图对象")
public class AdminEvaluationResultVO {

    @Schema(description = "结果 ID")
    private Long id;

    @Schema(description = "批次 ID")
    private Long batchId;

    @Schema(description = "批次名称")
    private String batchName;

    @Schema(description = "学生 ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "院系")
    private String department;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "课程成绩分数")
    private BigDecimal courseScore;

    @Schema(description = "科研成果分数")
    private BigDecimal researchScore;

    @Schema(description = "竞赛获奖分数")
    private BigDecimal competitionScore;

    @Schema(description = "综合素质分数")
    private BigDecimal qualityScore;

    @Schema(description = "总分")
    private BigDecimal totalScore;

    @Schema(description = "院系排名")
    private Integer departmentRank;

    @Schema(description = "专业排名")
    private Integer majorRank;

    @Schema(description = "奖项等级")
    private Integer awardLevel;

    @Schema(description = "奖学金金额")
    private BigDecimal awardAmount;

    @Schema(description = "结果状态")
    private Integer resultStatus;

    @Schema(description = "公示日期")
    private LocalDateTime publicityDate;

    @Schema(description = "确认日期")
    private LocalDateTime confirmDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
