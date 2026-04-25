package com.scholarship.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志展示对象。
 */
@Data
@Schema(description = "操作日志展示对象")
public class OperationLogVO {

    @Schema(description = "日志 ID")
    private Long id;

    @Schema(description = "操作人 ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作类型")
    private Integer operationType;

    @Schema(description = "操作类型文案")
    private String operationTypeLabel;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "操作 IP")
    private String operatorIp;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
