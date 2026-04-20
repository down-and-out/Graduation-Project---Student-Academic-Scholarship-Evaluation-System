package com.scholarship.service;

import java.util.List;

/**
 * 基础数据服务接口
 * <p>
 * 提供院系、专业等基础数据的查询
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface BasicDataService {

    /**
     * 获取院系列表（去重）
     *
     * @return 院系列表
     */
    List<String> getDepartments();

    /**
     * 获取专业列表（去重）
     *
     * @param department 院系（可选，按院系筛选）
     * @return 专业列表
     */
    List<String> getMajors(String department);
}
