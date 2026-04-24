package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("competition_award")
@Schema(description = "学科竞赛获奖")
public class CompetitionAward implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "获奖记录 ID")
    private Long id;

    @Schema(description = "学生档案 ID")
    private Long studentId;

    @Schema(description = "竞赛名称")
    private String competitionName;

    @Schema(description = "竞赛级别")
    private Integer competitionLevel;

    @Schema(description = "获奖级别")
    private Integer awardLevel;

    @Schema(description = "获奖等级")
    private Integer awardRank;

    @Schema(description = "获奖类型")
    private Integer awardType;

    @Schema(description = "成员排名")
    private Integer memberRank;

    @Schema(description = "指导老师")
    private String instructor;

    @Schema(description = "获奖日期")
    private LocalDate awardDate;

    @Schema(description = "颁发单位")
    private String issuingUnit;

    @Schema(description = "主办单位")
    private String organizer;

    @Schema(description = "团队成员")
    private String teamMembers;

    @Schema(description = "得分")
    private BigDecimal score;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    @Schema(description = "审核意见")
    private String auditComment;

    @Schema(description = "审核人 ID")
    private Long auditorId;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "历史证明材料字段")
    private String proofMaterials;

    @Schema(description = "附件地址")
    private String attachmentUrl;

    @Schema(description = "备注")
    private String remark;

    @TableLogic
    @Schema(description = "逻辑删除标记")
    private Integer deleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
