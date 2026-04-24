package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "批次奖项配置")
public class BatchAwardConfig implements Serializable {

    @NotNull(message = "奖项等级不能为空")
    @Schema(description = "奖项等级：1-特等 2-一等 3-二等 4-三等")
    private Integer awardLevel;

    @NotNull(message = "奖项比例不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "奖项比例必须大于 0")
    @Schema(description = "奖项比例，百分比数值")
    private BigDecimal ratio;

    @NotNull(message = "奖项金额不能为空")
    @DecimalMin(value = "0", message = "奖项金额不能为负数")
    @Schema(description = "奖项金额")
    private BigDecimal amount;
}
