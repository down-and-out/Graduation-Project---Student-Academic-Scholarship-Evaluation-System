package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 德育表现实体类
 * <p>
 * 对应数据库表：moral_performance
 * 存储学生的德育表现和综合素质评价信息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("moral_performance")
@Schema(description = "德育表现")
public class MoralPerformance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "记录 ID")
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
     * 表现类型
     * 1-志愿服务 2-社会实践 3-荣誉称号 4-学生干部 5-其他
     */
    @Schema(description = "表现类型：1-志愿服务 2-社会实践 3-荣誉称号 4-学生干部 5-其他")
    private Integer performanceType;

    /**
     * 表现名称
     */
    @Schema(description = "表现名称")
    private String performanceName;

    /**
     * 表现描述
     */
    @Schema(description = "表现描述")
    private String description;

    /**
     * 级别
     * 1-国家级 2-省级 3-校级 4-院级
     */
    @Schema(description = "级别：1-国家级 2-省级 3-校级 4-院级")
    private Integer level;

    /**
     * 得分
     */
    @Schema(description = "得分")
    private BigDecimal score;

    /**
     * 证明人
     */
    @Schema(description = "证明人")
    private String verifier;

    /**
     * 证明日期
     */
    @Schema(description = "证明日期")
    private LocalDate verifyDate;

    /**
     * 学年
     */
    @Schema(description = "学年")
    private String academicYear;

    /**
     * 学期
     * 1-第一学期 2-第二学期 3-全年
     */
    @Schema(description = "学期：1-第一学期 2-第二学期 3-全年")
    private Integer semester;

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
