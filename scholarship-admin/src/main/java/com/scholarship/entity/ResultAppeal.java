package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 结果异议实体。
 */
@Data
@TableName("result_appeal")
@Schema(description = "结果异议")
public class ResultAppeal implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "异议 ID")
    private Long id;

    @Schema(description = "结果 ID")
    private Long resultId;

    @Schema(description = "批次 ID")
    private Long batchId;

    @Schema(description = "学生 ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "异议类型")
    private Integer appealType;

    @Schema(description = "异议标题")
    private String appealTitle;

    @Schema(description = "异议原因")
    private String appealReason;

    @Schema(description = "异议内容")
    private String appealContent;

    @Schema(description = "附件路径")
    private String attachmentPath;

    @Schema(description = "异议状态")
    private Integer appealStatus;

    @Version
    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "处理意见")
    private String handleOpinion;

    @Schema(description = "处理人 ID")
    private Long handlerId;

    @Schema(description = "处理人姓名")
    private String handlerName;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @TableLogic
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
