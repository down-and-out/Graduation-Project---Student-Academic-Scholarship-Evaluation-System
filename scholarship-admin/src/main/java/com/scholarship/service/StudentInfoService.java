package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.StudentInfo;

/**
 * 研究生信息服务接口
 * <p>
 * 提供研究生信息的业务逻辑处理
 * 继承MyBatis-Plus的IService，获得基础的CRUD能力
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface StudentInfoService extends IService<StudentInfo> {

    /**
     * 分页查询研究生信息
     *
     * @param current  当前页
     * @param size     每页大小
     * @param keyword  搜索关键字（学号或姓名）
     * @param department 院系
     * @param status   学籍状态
     * @return 分页结果
     */
    IPage<StudentInfo> pageStudents(Long current, Long size, String keyword, String department, Integer status);

    /**
     * 根据用户ID获取研究生信息
     *
     * @param userId 用户ID
     * @return 研究生信息
     */
    StudentInfo getByUserId(Long userId);
}
