package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("research_project")
@Schema(description = "科研项目")
public class ResearchProject implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "项目 ID")
    private Long id;

    @Schema(description = "学生档案 ID")
    private Long studentId;

    @TableField(exist = false)
    @Schema(description = "学生学号")
    private String studentNo;

    @TableField(exist = false)
    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目编号")
    private String projectNo;

    @Schema(description = "项目类型")
    private Integer projectType;

    @Schema(description = "项目级别")
    private Integer projectLevel;

    @Schema(description = "项目来源")
    private String projectSource;

    @Schema(description = "负责人 ID")
    private Long leaderId;

    @Schema(description = "负责人姓名")
    private String leaderName;

    @Schema(description = "成员排名")
    private Integer memberRank;

    @Schema(description = "项目角色")
    private Integer projectRole;

    @Schema(description = "参与人员")
    private String participants;

    @Schema(description = "项目经费")
    private BigDecimal funding;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "项目状态")
    private Integer projectStatus;

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
