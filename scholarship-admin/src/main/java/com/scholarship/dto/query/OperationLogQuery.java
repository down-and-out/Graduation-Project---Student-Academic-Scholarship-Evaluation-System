package com.scholarship.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志查询参数
 * <p>
 * 用于分页查询系统操作日志
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogQuery extends PageQuery {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作状态：0-失败 1-成功
     */
    private Integer status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 关键词（模糊查询操作内容）
     */
    private String keyword;
}
