package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 科研专利实体类
 * <p>
 * 对应数据库表：research_patent
 * 存储研究生的专利申请和授权信息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Data
@TableName("research_patent")
@Schema(description = "科研专利")
public class ResearchPatent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 专利 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "专利 ID")
    private Long id;

    /**
     * 学生 ID
     */
    @Schema(description = "学生 ID")
    private Long studentId;

    /**
     * 专利名称
     */
    @Schema(description = "专利名称")
    private String patentName;

    /**
     * 专利号
     */
    @Schema(description = "专利号")
    private String patentNo;

    /**
     * 专利类型
     * 1-发明专利 2-实用新型 3-外观设计
     */
    @Schema(description = "专利类型：1-发明专利 2-实用新型 3-外观设计")
    private Integer patentType;

    /**
     * 申请人排名
     */
    @Schema(description = "申请人排名")
    private Integer applicantRank;

    /**
     * 申请日期
     */
    @Schema(description = "申请日期")
    private LocalDate applicationDate;

    /**
     * 授权日期
     */
    @Schema(description = "授权日期")
    private LocalDate authorizationDate;

    /**
     * 专利状态
     * 1-申请中 2-已授权 3-已驳回
     */
    @Schema(description = "专利状态：1-申请中 2-已授权 3-已驳回")
    private Integer patentStatus;

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
     * 证明材料路径（多个文件用逗号分隔）
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
