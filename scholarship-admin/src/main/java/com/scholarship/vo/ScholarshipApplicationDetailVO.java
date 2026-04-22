package com.scholarship.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 奖学金申请详情
 */
@Data
@Schema(description = "奖学金申请详情")
public class ScholarshipApplicationDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String applicationNo;
    private Long batchId;
    private Long studentId;
    private String selfEvaluation;
    private String remark;
    private Integer status;
    private BigDecimal totalScore;
    private String tutorOpinion;
    private LocalDateTime submitTime;
    private List<ApplicationAchievementVO> achievements = new ArrayList<>();
}
