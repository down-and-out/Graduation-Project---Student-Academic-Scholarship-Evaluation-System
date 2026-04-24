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
@TableName("research_patent")
@Schema(description = "科研专利")
public class ResearchPatent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "专利 ID")
    private Long id;

    @Schema(description = "学生档案 ID")
    private Long studentId;

    @Schema(description = "专利名称")
    private String patentName;

    @Schema(description = "专利类型")
    private Integer patentType;

    @Schema(description = "申请人排名")
    private Integer applicantRank;

    @Schema(description = "专利号")
    private String patentNo;

    @Schema(description = "发明人列表")
    private String inventors;

    @Schema(description = "发明人排名")
    private Integer inventorRank;

    @Schema(description = "申请人")
    private String applicant;

    @Schema(description = "申请日期")
    private LocalDate applicationDate;

    @Schema(description = "专利状态")
    private Integer patentStatus;

    @Schema(description = "附件地址")
    private String attachmentUrl;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    @Schema(description = "审核人 ID")
    private Long auditorId;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核意见")
    private String auditComment;

    @Schema(description = "得分")
    private BigDecimal score;

    @TableLogic
    @Schema(description = "逻辑删除标记")
    private Integer deleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;
}
