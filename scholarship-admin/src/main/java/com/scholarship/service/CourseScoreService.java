package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.dto.CourseScoreImportResult;
import com.scholarship.dto.query.CourseScoreQuery;
import com.scholarship.entity.CourseScore;
import com.scholarship.entity.StudentInfo;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 课程成绩服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface CourseScoreService extends IService<CourseScore> {

    /**
     * 查询学生的课程成绩列表
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID（可选，用于筛选特定学年的成绩）
     * @return 课程成绩列表
     */
    List<CourseScore> listByStudentId(Long studentId, Long batchId);

    /**
     * 计算学生的加权平均分
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID
     * @return 加权平均分
     */
    BigDecimal calculateWeightedAverage(Long studentId, Long batchId);

    /**
     * 计算学生的总学分
     *
     * @param studentId 学生 ID
     * @return 总学分
     */
    BigDecimal calculateTotalCredits(Long studentId);

    /**
     * 计算学生的平均绩点
     *
     * @param studentId 学生 ID
     * @return 平均绩点
     */
    BigDecimal calculateAverageGPA(Long studentId);

    /**
     * 批量计算学生的加权平均分
     *
     * @param studentIds 学生 ID 列表
     * @param batchId 批次 ID
     * @return 按学生 ID 分组的加权平均分
     */
    Map<Long, BigDecimal> mapWeightedAverageByStudentIds(List<Long> studentIds, Long batchId);

    /**
     * 批量计算学生的加权平均分（直接指定学年，避免重复查询批次信息）
     *
     * @param studentIds 学生 ID 列表
     * @param academicYear 学年
     * @return 按学生 ID 分组的加权平均分
     */
    Map<Long, BigDecimal> mapWeightedAverageByStudentIds(List<Long> studentIds, String academicYear);

    /**
     * 分页查询课程成绩
     *
     * @param query 查询参数
     * @return 分页结果
     */
    IPage<CourseScore> queryPage(CourseScoreQuery query);

    /**
     * 查询学生可选学年列表
     *
     * @param studentId 学生 ID
     * @return 学年列表，按倒序排列
     */
    List<String> listAcademicYearsByStudentId(Long studentId);

    /**
     * 根据查询条件获取成绩列表（用于导出）
     *
     * @param query 查询参数
     * @param maxRows 最大行数，>0 时追加 LIMIT，<=0 时不限制
     * @return 成绩列表
     */
    List<CourseScore> queryForExport(CourseScoreQuery query, int maxRows);

    /**
     * 导入学生课程成绩。
     *
     * @param inputStream Excel 输入流
     * @param originalFilename 原始文件名
     * @param studentInfo 学生信息
     * @return 导入条数
     */
    CourseScoreImportResult importScores(InputStream inputStream, String originalFilename, StudentInfo studentInfo) throws IOException;
}
