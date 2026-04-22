package com.scholarship.dto.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 申请成果提交项
 */
@Data
@Schema(description = "申请成果提交项")
public class ApplicationAchievementSubmitItem {

    @NotNull(message = "成果类型不能为空")
    @Schema(description = "成果类型：1-论文 2-专利 3-项目 4-竞赛")
    private Integer achievementType;

    @NotNull(message = "成果 ID 不能为空")
    @Schema(description = "成果 ID")
    private Long achievementId;
}
