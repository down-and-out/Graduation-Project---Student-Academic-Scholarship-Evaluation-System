package com.scholarship.dto.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 奖学金申请提交请求
 */
@Data
@Schema(description = "奖学金申请提交请求")
public class ScholarshipApplicationSubmitRequest {

    @NotNull(message = "批次 ID 不能为空")
    @Schema(description = "批次 ID")
    private Long batchId;

    @NotBlank(message = "自我评价不能为空")
    @Schema(description = "自我评价")
    private String selfEvaluation;

    @Schema(description = "备注")
    private String remark;

    @Valid
    @Schema(description = "关联成果列表")
    private List<ApplicationAchievementSubmitItem> achievements = new ArrayList<>();
}
