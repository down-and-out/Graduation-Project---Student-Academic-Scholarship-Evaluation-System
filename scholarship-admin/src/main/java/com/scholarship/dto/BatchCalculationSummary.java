package com.scholarship.dto;

import lombok.Data;

@Data
public class BatchCalculationSummary {

    private Long batchId;

    private int processedCount;

    private int writtenCount;

    private int pageCount;
}
