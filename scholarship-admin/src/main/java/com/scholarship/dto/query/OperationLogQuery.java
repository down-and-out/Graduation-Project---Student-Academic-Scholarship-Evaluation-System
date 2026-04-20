package com.scholarship.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogQuery extends PageQuery {

    private Long userId;

    private String operationType;

    private List<String> operationTypes;

    private String module;

    private Integer status;

    private String startTime;

    private String endTime;

    private String keyword;
}
