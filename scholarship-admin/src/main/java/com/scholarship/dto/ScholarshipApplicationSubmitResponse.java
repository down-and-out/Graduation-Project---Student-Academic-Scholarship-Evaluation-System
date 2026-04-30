package com.scholarship.dto;

import lombok.Data;

@Data
public class ScholarshipApplicationSubmitResponse {

    private Long applicationId;

    private String applicationNo;

    private Integer status;

    private boolean created;

    private boolean idempotent;

    private String message;
}
