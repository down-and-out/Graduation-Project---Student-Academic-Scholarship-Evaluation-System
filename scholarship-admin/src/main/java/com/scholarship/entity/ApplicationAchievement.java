package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 申请成果关联实体类
 * <p>
 * 对应数据库表：application_achievement
 * 存储奖学金申请与科研成果的关联关系
 * </p>
 *
 * 数据库索引建议：
 * <pre>
 * CREATE INDEX idx_application_id ON application_achievement(application_id);
 * CREATE INDEX idx_achievement_id ON application_achievement(achievement_id);
 * </pre>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("application_achievement")
@Schema(description = "申请成果关联")
public class ApplicationAchievement implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "关联 ID")
    private Long id;

    /**
     * 申请 ID
     */
    @NotNull(message = "申请 ID 不能为空")
    @Schema(description = "申请 ID")
    private Long applicationId;

    /**
     * 成果类型
     * 1-论文 2-专利 3-项目 4-竞赛
     */
    @NotNull(message = "成果类型不能为空")
    @Schema(description = "成果类型：1-论文 2-专利 3-项目 4-竞赛")
    private Integer achievementType;

    /**
     * 成果 ID
     */
    @NotNull(message = "成果 ID 不能为空")
    @Schema(description = "成果 ID")
    private Long achievementId;

    /**
     * 认定分数
     */
    @Schema(description = "认定分数")
    private BigDecimal score;

    /**
     * 评分说明
     */
    @Schema(description = "评分说明")
    private String scoreComment;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;
}
