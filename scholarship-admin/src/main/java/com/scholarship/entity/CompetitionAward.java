package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学科竞赛获奖实体类
 * <p>
 * 对应数据库表：competition_award
 * 存储研究生参加学科竞赛获得的奖项信息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Data
@TableName("competition_award")
@Schema(description = "学科竞赛获奖")
public class CompetitionAward implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 获奖 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "获奖 ID")
    private Long id;

    /**
     * 学生 ID
     */
    @Schema(description = "学生 ID")
    private Long studentId;

    /**
     * 竞赛名称
     */
    @Schema(description = "竞赛名称")
    private String competitionName;

    /**
     * 竞赛级别
     * 1-国家级 2-省级 3-校级
     */
    @Schema(description = "竞赛级别：1-国家级 2-省级 3-校级")
    private Integer competitionLevel;

    /**
     * 获奖等级
     * 1-特等奖 2-一等奖 3-二等奖 4-三等奖 5-优秀奖
     */
    @Schema(description = "获奖等级：1-特等奖 2-一等奖 3-二等奖 4-三等奖 5-优秀奖")
    private Integer awardLevel;

    /**
     * 获奖名次
     */
    @Schema(description = "获奖名次")
    private String awardRank;

    /**
     * 团队/个人
     * 1-个人 2-团队
     */
    @Schema(description = "团队/个人：1-个人 2-团队")
    private Integer awardType;

    /**
     * 团队成员排名
     */
    @Schema(description = "团队成员排名")
    private Integer memberRank;

    /**
     * 指导老师
     */
    @Schema(description = "指导老师")
    private String instructor;

    /**
     * 获奖日期
     */
    @Schema(description = "获奖日期")
    private LocalDate awardDate;

    /**
     * 颁发单位
     */
    @Schema(description = "颁发单位")
    private String issuingUnit;

    /**
     * 获得分数
     */
    @Schema(description = "获得分数")
    private BigDecimal score;

    /**
     * 审核状态
     * 0-待审核 1-审核通过 2-审核驳回
     */
    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核驳回")
    private Integer auditStatus;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String auditComment;

    /**
     * 审核人 ID
     */
    @Schema(description = "审核人 ID")
    private Long auditorId;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    /**
     * 证明材料路径
     */
    @Schema(description = "证明材料路径")
    private String proofMaterials;

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
