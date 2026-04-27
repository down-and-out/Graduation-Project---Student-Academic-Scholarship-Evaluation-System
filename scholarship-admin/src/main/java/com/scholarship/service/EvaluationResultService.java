package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.dto.param.EvaluationResultAdjustRequest;
import com.scholarship.dto.query.EvaluationResultQuery;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.vo.AdminEvaluationResultVO;
import com.scholarship.vo.EvaluationResultExportVO;

import java.util.List;

/**
 * 评定结果服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface EvaluationResultService extends IService<EvaluationResult> {

    /**
     * 分页查询评定结果
     *
     * @param current   当前页
     * @param size      每页大小
     * @param batchId   批次 ID
     * @param academicYear 学年
     * @param semester 学期
     * @param studentId 学生 ID
     * @param status    状态（1-公示中, 2-已确定, 3-有异议）
     * @param keyword   关键词（学号/姓名模糊查询）
     * @return 分页结果
     */
    IPage<EvaluationResult> pageResults(Long current, Long size, Long batchId, String academicYear, Integer semester,
                                        Long studentId, Integer status, String keyword);

    IPage<AdminEvaluationResultVO> pageAdminResults(Long current, Long size, Long batchId, String academicYear,
                                                    Integer semester, Long studentId, Integer status, String keyword);

    /**
     * 根据查询参数分页查询
     *
     * @param query 查询参数
     * @return 分页结果
     */
    IPage<EvaluationResult> queryPage(EvaluationResultQuery query);

    /**
     * 获取学生的评定结果
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID（可选，不传则获取最新的）
     * @return 评定结果
     */
    EvaluationResult getStudentResult(Long studentId, Long batchId);

    AdminEvaluationResultVO getAdminResultById(Long id);

    boolean adjustResult(Long id, EvaluationResultAdjustRequest request);

    /**
     * 确认评定结果
     *
     * @param id 结果 ID
     * @return 是否成功
     */
    boolean confirmResult(Long id);

    /**
     * 标记评定结果有异议
     *
     * @param id 结果 ID
     * @return 是否成功
     */
    boolean objectResult(Long id);

    /**
     * 获取批次排名列表
     *
     * @param batchId 批次 ID
     * @param type 排名类型（department-院系排名，major-专业排名）
     * @return 排名列表
     */
    List<EvaluationResult> getBatchRanks(Long batchId, String type);

    /**
     * 导出批次评定结果
     *
     * @param batchId 批次 ID（可选）
     * @param academicYear 学年（可选）
     * @param semester 学期（可选）
     * @return 导出数据列表
     */
    List<EvaluationResultExportVO> exportBatchResults(Long batchId, String academicYear, Integer semester);
}
