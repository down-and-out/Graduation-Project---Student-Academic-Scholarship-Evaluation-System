package com.scholarship.service;

import com.scholarship.dto.StudentImportDTO;

import java.util.List;
import java.util.Map;

/**
 * 批量导入服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface BatchImportService {

    /**
     * 批量导入学生信息
     *
     * @param students 学生信息列表
     * @return 导入结果，包含成功数和失败详情
     */
    Map<String, Object> importStudents(List<StudentImportDTO> students);

    /**
     * 获取学生导入模板
     *
     * @return 模板文件字节数组
     */
    byte[] getImportTemplate();
}
