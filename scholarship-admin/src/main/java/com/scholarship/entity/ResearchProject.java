package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 科研项目实体类
 * <p>
 * 对应数据库表：research_project
 * 存储研究生参与的科研项目信息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Data
@TableName("research_project")
@Schema(description = "科研项目")
public class ResearchProject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "项目 ID")
    private Long id;

    /**
     * 学生 ID
     */
    @Schema(description = "学生 ID")
    private Long studentId;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectName;

    /**
     * 项目编号
     */
    @Schema(description = "项目编号")
    private String projectNo;

    /**
     * 项目类型
     * 1-国家级 2-省部级 3-市厅级 4-校级
     */
    @Schema(description = "项目类型：1-国家级 2-省部级 3-市厅级 4-校级")
    private Integer projectType;

    /**
     * 项目来源
     */
    @Schema(description = "项目来源")
    private String projectSource;

    /**
     * 项目负责人 ID
     */
    @Schema(description = "项目负责人 ID")
    private Long leaderId;

    /**
     * 项目负责人姓名
     */
    @Schema(description = "项目负责人姓名")
    private String leaderName;

    /**
     * 成员排名
     */
    @Schema(description = "成员排名")
    private Integer memberRank;

    /**
     * 项目经费（万元）
     */
    @Schema(description = "项目经费（万元）")
    private BigDecimal funding;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    private LocalDate endDate;

    /**
     * 项目状态
     * 1-在研 2-已结题 3-已终止
     */
    @Schema(description = "项目状态：1-在研 2-已结题 3-已终止")
    private Integer projectStatus;

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
