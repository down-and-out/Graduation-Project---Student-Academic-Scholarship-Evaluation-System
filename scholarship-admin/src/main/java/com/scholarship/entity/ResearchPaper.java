package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 科研论文实体类
 * <p>
 * 对应数据库表：research_paper
 * 存储研究生发表的论文信息
 * </p>
 *
 * 数据库索引建议：
 * <pre>
 * CREATE INDEX idx_student_id ON research_paper(student_id);
 * CREATE INDEX idx_status ON research_paper(status);
 * </pre>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("research_paper")
@Schema(description = "科研论文")
public class ResearchPaper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    /**
     * 学生 ID
     */
    @NotNull(message = "学生 ID 不能为空")
    @Schema(description = "学生 ID")
    private Long studentId;

    /**
     * 论文标题
     */
    @NotBlank(message = "论文标题不能为空")
    @Schema(description = "论文标题")
    private String paperTitle;

    /**
     * 作者列表
     */
    @Schema(description = "作者列表")
    private String authors;

    /**
     * 学生作者排名：1-第一作者 2-第二作者 3-通讯作者
     */
    @NotNull(message = "学生作者排名不能为空")
    @Schema(description = "学生作者排名")
    private Integer authorRank;

    /**
     * 期刊名称
     */
    @Schema(description = "期刊名称")
    private String journalName;

    /**
     * 期刊级别：1-SCI 一区 2-SCI 二区 3-SCI 三区 4-SCI 四区 5-EI 6-核心期刊 7-普通期刊
     */
    @Schema(description = "期刊级别")
    private Integer journalLevel;

    /**
     * 影响因子
     */
    @Schema(description = "影响因子")
    private BigDecimal impactFactor;

    /**
     * 发表日期
     */
    @Schema(description = "发表日期")
    private LocalDate publicationDate;

    /**
     * 卷号
     */
    @Schema(description = "卷号")
    private String volume;

    /**
     * 期号
     */
    @Schema(description = "期号")
    private String issue;

    /**
     * 页码范围
     */
    @Schema(description = "页码范围")
    private String pages;

    /**
     * DOI 编号
     */
    @Schema(description = "DOI 编号")
    private String doi;

    /**
     * 收录情况（SCI/EI/ISTP 等）
     */
    @Schema(description = "收录情况")
    private String indexing;

    /**
     * 附件 URL（论文 PDF）
     */
    @Schema(description = "附件 URL")
    private String attachmentUrl;

    /**
     * 审核状态：0-待审核 1-导师审核通过 2-院系审核通过 3-审核不通过
     */
    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态")
    private Integer status;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String reviewComment;

    /**
     * 审核人 ID
     */
    @Schema(description = "审核人 ID")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

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
