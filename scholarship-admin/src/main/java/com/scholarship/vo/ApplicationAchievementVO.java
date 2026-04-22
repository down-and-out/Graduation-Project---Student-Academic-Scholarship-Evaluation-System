package com.scholarship.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 申请成果展示对象
 */
@Data
@Schema(description = "申请成果展示对象")
public class ApplicationAchievementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关联记录 ID")
    private Long id;

    @Schema(description = "成果类型：1-论文 2-专利 3-项目 4-竞赛")
    private Integer achievementType;

    @Schema(description = "成果 ID")
    private Long achievementId;

    @Schema(description = "成果名称")
    private String title;

    @Schema(description = "成果说明")
    private String subtitle;

    @Schema(description = "作者信息")
    private String authors;

    @Schema(description = "得分")
    private BigDecimal score;

    @Schema(description = "评分说明")
    private String scoreComment;
}
