package com.scholarship.dto.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "管理员调整评定结果请求")
public class EvaluationResultAdjustRequest {

    @NotNull(message = "奖项等级不能为空")
    @Min(value = 1, message = "奖项等级必须在 1-5 之间")
    @Max(value = 5, message = "奖项等级必须在 1-5 之间")
    @Schema(description = "调整后的奖项等级，1-特等奖 2-一等奖 3-二等奖 4-三等奖 5-未获奖", example = "2")
    private Integer awardLevel;

    @NotBlank(message = "调整原因不能为空")
    @Size(max = 255, message = "调整原因长度不能超过 255 个字符")
    @Schema(description = "调整原因", example = "经复核后修正奖项等级")
    private String reason;
}
