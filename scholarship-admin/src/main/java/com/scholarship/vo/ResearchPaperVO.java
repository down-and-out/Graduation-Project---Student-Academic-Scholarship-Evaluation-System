package com.scholarship.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 科研论文 VO 类
 * <p>
 * 用于前端展示，包含学生信息和论文信息的组合数据
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@Schema(description = "科研论文展示对象")
public class ResearchPaperVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 论文 ID
     */
    @Schema(description = "论文 ID")
    private Long id;

    /**
     * 学生 ID
     */
    @Schema(description = "学生 ID")
    private Long studentId;

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentNo;

    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名")
    private String studentName;

    /**
     * 成果类型（paper-论文, patent-专利，project-项目）
     */
    @Schema(description = "成果类型")
    private String type = "paper";

    /**
     * 成果名称（论文标题）
     */
    @Schema(description = "成果名称")
    private String title;

    /**
     * 级别（期刊级别）
     */
    @Schema(description = "级别")
    private String level;

    /**
     * 分值（申请分值）
     */
    @Schema(description = "分值")
    private BigDecimal score;

    /**
     * 作者/发明人
     */
    @Schema(description = "作者列表")
    private String authors;

    /**
     * 学生作者排名
     */
    @Schema(description = "学生作者排名")
    private Integer authorRank;

    /**
     * 发表/授权日期
     */
    @Schema(description = "发表日期")
    private LocalDate date;

    /**
     * 提交时间
     */
    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    /**
     * 审核状态：0-待审核 1-导师审核通过 2-院系审核通过 3-审核不通过
     */
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
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
