package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.event.CacheEvictionEvent;
import com.scholarship.common.event.CacheEvictionOperation;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.support.CacheConstants;
import com.scholarship.common.support.CursorPageHelper;
import com.scholarship.config.ScholarshipProperties;
import com.scholarship.dto.param.EvaluationResultAdjustRequest;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.enums.AwardLevelEnum;
import com.scholarship.enums.ResultStatusEnum;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.service.EvaluationBatchService;
import com.scholarship.service.EvaluationResultService;
import com.scholarship.vo.AdminEvaluationResultVO;
import com.scholarship.vo.EvaluationResultExportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评定结果服务实现类
 *
 * 功能说明：
 * - 分页查询评定结果（支持多种筛选条件）
 * - 分页查询评定结果（管理员视角，返回 VO）
 * - 获取学生评定结果
 * - 获取管理员视角结果详情
 * - 确认评定结果
 * - 标记异议
 * - 获取批次排名列表
 * - 导出评定结果
 * - 调整评定结果
 *
 * 缓存说明：
 * - 管理员视角结果分页：使用缓存（key 根据分页参数和筛选条件生成）
 * - 学生结果：按 studentId 和 batchId 缓存
 * - 管理员结果详情：按 resultId 缓存
 * - 批次排名：按 batchId 和 type 缓存
 * - 操作后发布缓存清除事件
 *
 * 事务说明：
 * - 确认、标记异议、调整操作使用 @Transactional 保证一致性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationResultServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResult>
        implements EvaluationResultService {

    private final EvaluationResultMapper evaluationResultMapper;
    private final EvaluationBatchService evaluationBatchService;
    private final ApplicationEventPublisher eventPublisher;
    private final ScholarshipProperties scholarshipProperties;

    /**
     * 分页查询评定结果（基础查询）
     *
     * @param current     当前页码
     * @param size        每页条数
     * @param batchId     批次ID（精确筛选）
     * @param academicYear 学年 + 学期（模糊筛选，通过批次匹配）
     * @param semester    学期
     * @param studentId   学生ID
     * @param status      结果状态
     * @param keyword     关键词（学号/姓名模糊搜索）
     * @return 分页后的评定结果列表
     */
    @Override
    public IPage<EvaluationResult> pageResults(Long current, Long size, Long batchId, String academicYear,
                                               Integer semester, Long studentId, Integer status, String keyword) {
        // 限制最大偏移量，防止查询过于耗时
        CursorPageHelper.validateOffset(current, size, scholarshipProperties.getEvaluation().getMaxOffsetRows());
        Page<EvaluationResult> page = new Page<>(current, size);
        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();

        // 批次ID精确筛选 或 学年+学期模糊筛选（通过批次匹配）
        if (batchId != null) {
            wrapper.eq(EvaluationResult::getBatchId, batchId);
        } else if (StringUtils.isNotBlank(academicYear) || semester != null) {
            List<Long> matchedBatchIds = resolveBatchIds(academicYear, semester);
            if (matchedBatchIds.isEmpty()) {
                return new Page<>(current, size, 0);
            }
            wrapper.in(EvaluationResult::getBatchId, matchedBatchIds);
        }

        // 学生ID筛选
        if (studentId != null) {
            wrapper.eq(EvaluationResult::getStudentId, studentId);
        }

        // 结果状态筛选
        if (status != null) {
            wrapper.eq(EvaluationResult::getResultStatus, status);
        }

        // 关键词搜索（学号或姓名模糊匹配）
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(EvaluationResult::getStudentNo, keyword)
                    .or()
                    .like(EvaluationResult::getStudentName, keyword));
        }

        // 按总分倒序排列
        wrapper.orderByDesc(EvaluationResult::getTotalScore);
        return evaluationResultMapper.selectPage(page, wrapper);
    }

    /**
     * 分页查询评定结果（管理员视角）
     * 转换结果为 VO，包含批次名称等信息
     * 使用缓存
     */
    @Override
    @Cacheable(
            value = CacheConstants.EVAL_PAGE,
            key = "T(com.scholarship.common.support.CacheConstants).evalPageKey(#current, #size, #batchId, #academicYear, #semester, #studentId, #status, #keyword)",
            unless = "#result == null || #result.records == null || #result.records.isEmpty()"
    )
    public IPage<AdminEvaluationResultVO> pageAdminResults(Long current, Long size, Long batchId, String academicYear,
                                                           Integer semester, Long studentId, Integer status,
                                                           String keyword) {
        // 1. 执行基础分页查询
        IPage<EvaluationResult> pageResult = pageResults(
                current,
                size,
                batchId,
                academicYear,
                semester,
                studentId,
                status,
                keyword
        );
        // 2. 批量加载批次名称
        Map<Long, String> batchNameMap = loadBatchNameMap(pageResult.getRecords());

        // 3. 转换为 VO 分页结果
        Page<AdminEvaluationResultVO> adminPage = new Page<>(
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getTotal()
        );
        adminPage.setRecords(pageResult.getRecords().stream()
                .map(item -> toAdminVO(item, batchNameMap))
                .toList());
        return adminPage;
    }

    /**
     * 获取学生评定结果
     * 按 studentId 和 batchId 查询，使用缓存
     *
     * @param studentId 学生ID
     * @param batchId   批次ID（可选，为空时查询最新结果）
     * @return 评定结果
     */
    @Override
    @Cacheable(value = CacheConstants.EVAL_STUDENT, key = "'student:' + #studentId + ':batch:' + (#batchId != null ? #batchId.toString() : 'latest')")
    public EvaluationResult getStudentResult(Long studentId, Long batchId) {
        log.debug("Load evaluation result for studentId={}, batchId={}", studentId, batchId);

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationResult::getStudentId, studentId);

        Page<EvaluationResult> page = new Page<>(1, 1, false);
        if (batchId != null) {
            // 指定批次查询
            wrapper.eq(EvaluationResult::getBatchId, batchId);
            return evaluationResultMapper.selectPage(page, wrapper).getRecords().stream()
                    .findFirst()
                    .orElse(null);
        }

        // 不指定批次，查询最新结果（按批次ID和结果ID倒序）
        wrapper.orderByDesc(EvaluationResult::getBatchId)
                .orderByDesc(EvaluationResult::getId);
        return evaluationResultMapper.selectPage(page, wrapper).getRecords().stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取管理员视角结果详情
     * 使用缓存
     */
    @Override
    @Cacheable(value = CacheConstants.EVAL_ADMIN, key = "#id")
    public AdminEvaluationResultVO getAdminResultById(Long id) {
        EvaluationResult result = getById(id);
        if (result == null) {
            return null;
        }
        return toAdminVO(result, loadBatchNameMap(List.of(result)));
    }

    /**
     * 确认评定结果
     * 将结果状态改为已确认
     */
    @Override
    @Transactional
    public boolean confirmResult(Long id) {
        log.info("Confirm evaluation result, id={}", id);

        EvaluationResult result = getById(id);
        if (result == null) {
            throw new BusinessException("评定结果不存在");
        }

        result.setResultStatus(ResultStatusEnum.CONFIRMED.getCode());
        boolean success = updateById(result);
        evictResultCaches(result.getBatchId(), id);
        return success;
    }

    /**
     * 标记异议
     * 将结果状态改为有异议
     */
    @Override
    @Transactional
    public boolean objectResult(Long id) {
        log.info("Mark evaluation result objected, id={}", id);

        EvaluationResult result = getById(id);
        if (result == null) {
            throw new BusinessException("评定结果不存在");
        }

        result.setResultStatus(ResultStatusEnum.OBJECTED.getCode());
        boolean success = updateById(result);
        evictResultCaches(result.getBatchId(), id);
        return success;
    }

    /**
     * 获取批次排名列表
     * 按总分倒序排列，使用缓存
     *
     * @param batchId 批次ID
     * @param type    排名类型（department=院系排名，major=专业排名）
     */
    @Override
    @Cacheable(value = CacheConstants.EVAL_RANK, key = "#batchId + ':' + #type", unless = "#result.isEmpty()")
    public List<EvaluationResult> getBatchRanks(Long batchId, String type) {
        log.debug("Load batch ranks, batchId={}, type={}", batchId, type);

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationResult::getBatchId, batchId)
                .orderByDesc(EvaluationResult::getTotalScore);

        if ("department".equals(type)) {
            // 院系排名：按院系排名字段正序
            wrapper.orderByAsc(EvaluationResult::getDepartmentRank);
        } else {
            // 专业排名：按专业排名字段正序
            wrapper.orderByAsc(EvaluationResult::getMajorRank);
        }

        return list(wrapper);
    }

    /**
     * 导出评定结果
     * 用于生成 Excel 导出数据
     *
     * @param batchId      批次ID
     * @param academicYear 学年
     * @param semester    学期
     * @param maxRows     最大导出行数
     * @return 导出数据列表
     */
    @Override
    public List<EvaluationResultExportVO> exportBatchResults(Long batchId, String academicYear, Integer semester, int maxRows) {
        log.info("Export evaluation results, batchId={}, academicYear={}, semester={}, maxRows={}", batchId, academicYear, semester, maxRows);

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();
        if (batchId != null) {
            wrapper.eq(EvaluationResult::getBatchId, batchId);
        } else if (StringUtils.isNotBlank(academicYear) || semester != null) {
            List<Long> matchedBatchIds = resolveBatchIds(academicYear, semester);
            if (matchedBatchIds.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in(EvaluationResult::getBatchId, matchedBatchIds);
        }
        wrapper.orderByDesc(EvaluationResult::getTotalScore);
        if (maxRows > 0) {
            wrapper.last("LIMIT " + maxRows);
        }

        List<EvaluationResult> results = list(wrapper);
        List<EvaluationResultExportVO> exportData = new ArrayList<>();
        int index = 1;
        for (EvaluationResult result : results) {
            EvaluationResultExportVO vo = new EvaluationResultExportVO();
            vo.setIndex(index++);
            vo.setStudentNo(result.getStudentNo());
            vo.setStudentName(result.getStudentName());
            vo.setDepartment(result.getDepartment());
            vo.setMajor(result.getMajor());
            vo.setCourseScore(result.getCourseScore());
            vo.setResearchScore(result.getResearchScore());
            vo.setCompetitionScore(result.getCompetitionScore());
            vo.setQualityScore(result.getQualityScore());
            vo.setTotalScore(result.getTotalScore());
            vo.setDepartmentRank(result.getDepartmentRank());
            vo.setMajorRank(result.getMajorRank());
            vo.setAwardLevelName(AwardLevelEnum.getDescription(result.getAwardLevel()));
            vo.setAwardAmount(result.getAwardAmount());
            vo.setResultStatusName(ResultStatusEnum.getDescription(result.getResultStatus()));
            exportData.add(vo);
        }

        return exportData;
    }

    /**
     * 调整评定结果
     * 用于异议处理
     */
    @Override
    @Transactional
    public boolean adjustResult(Long id, EvaluationResultAdjustRequest request) {
        EvaluationResult result = getById(id);
        if (result == null) {
            throw new BusinessException("评定结果不存在");
        }

        result.setAwardLevel(request.getAwardLevel());
        result.setRemark(request.getReason().trim());
        boolean success = updateById(result);
        evictResultCaches(result.getBatchId(), id);
        return success;
    }

    // ========== 私有工具方法 ==========

    /**
     * 根据学年和学期解析匹配的批次ID列表
     */
    private List<Long> resolveBatchIds(String academicYear, Integer semester) {
        LambdaQueryWrapper<EvaluationBatch> batchWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(academicYear)) {
            batchWrapper.eq(EvaluationBatch::getAcademicYear, academicYear.trim());
        }
        if (semester != null) {
            batchWrapper.eq(EvaluationBatch::getSemester, semester);
        }

        return evaluationBatchService.list(batchWrapper).stream()
                .map(EvaluationBatch::getId)
                .filter(id -> id != null)
                .toList();
    }

    /**
     * 批量加载批次名称（以结果中的批次ID为key）
     */
    private Map<Long, String> loadBatchNameMap(List<EvaluationResult> results) {
        Set<Long> batchIds = results.stream()
                .map(EvaluationResult::getBatchId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (batchIds.isEmpty()) {
            return Map.of();
        }

        return evaluationBatchService.listByIds(batchIds).stream()
                .collect(Collectors.toMap(
                        EvaluationBatch::getId,
                        EvaluationBatch::getBatchName,
                        (left, right) -> left,
                        HashMap::new
                ));
    }

    /**
     * 清除结果相关缓存
     */
    private void evictResultCaches(Long batchId, Long resultId) {
        eventPublisher.publishEvent(new CacheEvictionEvent(
                this,
                CacheEvictionOperation.EVICT_EVALUATION_RESULTS_FOR_BATCH,
                batchId
        ));
        eventPublisher.publishEvent(new CacheEvictionEvent(
                this,
                CacheEvictionOperation.EVICT_ADMIN_RESULT,
                resultId
        ));
    }

    /**
     * 将结果实体转换为管理员 VO
     */
    private AdminEvaluationResultVO toAdminVO(EvaluationResult result, Map<Long, String> batchNameMap) {
        AdminEvaluationResultVO vo = new AdminEvaluationResultVO();
        vo.setId(result.getId());
        vo.setBatchId(result.getBatchId());
        vo.setBatchName(batchNameMap.get(result.getBatchId()));
        vo.setStudentId(result.getStudentId());
        vo.setStudentName(result.getStudentName());
        vo.setStudentNo(result.getStudentNo());
        vo.setDepartment(result.getDepartment());
        vo.setMajor(result.getMajor());
        vo.setCourseScore(result.getCourseScore());
        vo.setResearchScore(result.getResearchScore());
        vo.setCompetitionScore(result.getCompetitionScore());
        vo.setQualityScore(result.getQualityScore());
        vo.setTotalScore(result.getTotalScore());
        vo.setDepartmentRank(result.getDepartmentRank());
        vo.setMajorRank(result.getMajorRank());
        vo.setAwardLevel(result.getAwardLevel());
        vo.setAwardAmount(result.getAwardAmount());
        vo.setResultStatus(result.getResultStatus());
        vo.setPublicityDate(result.getPublicityDate());
        vo.setConfirmDate(result.getConfirmDate());
        vo.setRemark(result.getRemark());
        vo.setCreateTime(result.getCreateTime());
        vo.setUpdateTime(result.getUpdateTime());
        return vo;
    }
}
