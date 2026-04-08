package com.scholarship.dto.query;

import lombok.Data;

/**
 * 分页查询基类
 * <p>
 * 所有分页查询 DTO 的父类
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
public class PageQuery {

    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 是否升序
     */
    private Boolean asc = false;
}
