package com.scholarship.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 德育表现查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MoralPerformanceQuery extends PageQuery {

    /**
     * 单个学生 ID。
     */
    private Long studentId;

    /**
     * 多个学生 ID，用于导师权限范围过滤。
     */
    private List<Long> studentIds;

    /**
     * 表现类型：1-志愿服务，2-社会实践，3-荣誉称号，4-学生干部，5-其他。
     */
    private Integer performanceType;

    /**
     * 学年。
     */
    private String academicYear;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核驳回。
     */
    private Integer auditStatus;
}
