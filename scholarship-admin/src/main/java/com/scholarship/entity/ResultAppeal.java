package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 结果异议实体类
 * <p>
 * 对应数据库表：result_appeal
 * 存储学生对评定结果的异议申诉信息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Data
@TableName("result_appeal")
@Schema(description = "结果异议")
public class ResultAppeal implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 异议 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "异议 ID")
    private Long id;

    /**
     * 结果 ID
     */
    @Schema(description = "结果 ID")
    private Long resultId;

    /**
     * 批次 ID
     */
    @Schema(description = "批次 ID")
    private Long batchId;

    /**
     * 学生 ID
     */
    @Schema(description = "学生 ID")
    private Long studentId;

    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名")
    private String studentName;

    /**
     * 异议类型
     * 1-分数错误 2-材料遗漏 3-计算错误 4-其他
     */
    @Schema(description = "异议类型：1-分数错误 2-材料遗漏 3-计算错误 4-其他")
    private Integer appealType;

    /**
     * 异议标题
     */
    @Schema(description = "异议标题")
    private String appealTitle;

    /**
     * 异议内容
     */
    @Schema(description = "异议内容")
    private String appealContent;

    /**
     * 附件路径
     */
    @Schema(description = "附件路径")
    private String attachmentPath;

    /**
     * 申诉状态
     * 1-待处理 2-处理中 3-已处理 4-已驳回
     */
    @Schema(description = "申诉状态：1-待处理 2-处理中 3-已处理 4-已驳回")
    private Integer appealStatus;

    /**
     * 处理人 ID
     */
    @Schema(description = "处理人 ID")
    private Long handlerId;

    /**
     * 处理人姓名
     */
    @Schema(description = "处理人姓名")
    private String handlerName;

    /**
     * 处理结果
     */
    @Schema(description = "处理结果")
    private String handleResult;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

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

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
