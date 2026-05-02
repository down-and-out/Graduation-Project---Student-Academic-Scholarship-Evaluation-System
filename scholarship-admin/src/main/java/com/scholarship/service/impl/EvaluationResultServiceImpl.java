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
 * Evaluation result service implementation.
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

    @Override
    public IPage<EvaluationResult> pageResults(Long current, Long size, Long batchId, String academicYear,
                                               Integer semester, Long studentId, Integer status, String keyword) {
        CursorPageHelper.validateOffset(current, size, scholarshipProperties.getEvaluation().getMaxOffsetRows());
        Page<EvaluationResult> page = new Page<>(current, size);
        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();

        if (batchId != null) {
            wrapper.eq(EvaluationResult::getBatchId, batchId);
        } else if (StringUtils.isNotBlank(academicYear) || semester != null) {
            List<Long> matchedBatchIds = resolveBatchIds(academicYear, semester);
            if (matchedBatchIds.isEmpty()) {
                return new Page<>(current, size, 0);
            }
            wrapper.in(EvaluationResult::getBatchId, matchedBatchIds);
        }

        if (studentId != null) {
            wrapper.eq(EvaluationResult::getStudentId, studentId);
        }

        if (status != null) {
            wrapper.eq(EvaluationResult::getResultStatus, status);
        }

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(EvaluationResult::getStudentNo, keyword)
                    .or()
                    .like(EvaluationResult::getStudentName, keyword));
        }

        wrapper.orderByDesc(EvaluationResult::getTotalScore);
        return evaluationResultMapper.selectPage(page, wrapper);
    }

    @Override
    @Cacheable(
            value = CacheConstants.EVAL_PAGE,
            key = "T(com.scholarship.common.support.CacheConstants).evalPageKey(#current, #size, #batchId, #academicYear, #semester, #studentId, #status, #keyword)",
            unless = "#result == null || #result.records == null || #result.records.isEmpty()"
    )
    public IPage<AdminEvaluationResultVO> pageAdminResults(Long current, Long size, Long batchId, String academicYear,
                                                           Integer semester, Long studentId, Integer status,
                                                           String keyword) {
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
        Map<Long, String> batchNameMap = loadBatchNameMap(pageResult.getRecords());

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

    @Override
    @Cacheable(value = CacheConstants.EVAL_STUDENT, key = "'student:' + #studentId + ':batch:' + (#batchId != null ? #batchId.toString() : 'latest')")
    public EvaluationResult getStudentResult(Long studentId, Long batchId) {
        log.debug("Load evaluation result for studentId={}, batchId={}", studentId, batchId);

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationResult::getStudentId, studentId);

        Page<EvaluationResult> page = new Page<>(1, 1, false);
        if (batchId != null) {
            wrapper.eq(EvaluationResult::getBatchId, batchId);
            return evaluationResultMapper.selectPage(page, wrapper).getRecords().stream()
                    .findFirst()
                    .orElse(null);
        }

        wrapper.orderByDesc(EvaluationResult::getBatchId)
                .orderByDesc(EvaluationResult::getId);
        return evaluationResultMapper.selectPage(page, wrapper).getRecords().stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Cacheable(value = CacheConstants.EVAL_ADMIN, key = "#id")
    public AdminEvaluationResultVO getAdminResultById(Long id) {
        EvaluationResult result = getById(id);
        if (result == null) {
            return null;
        }
        return toAdminVO(result, loadBatchNameMap(List.of(result)));
    }

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

    @Override
    @Cacheable(value = CacheConstants.EVAL_RANK, key = "#batchId + ':' + #type", unless = "#result.isEmpty()")
    public List<EvaluationResult> getBatchRanks(Long batchId, String type) {
        log.debug("Load batch ranks, batchId={}, type={}", batchId, type);

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationResult::getBatchId, batchId)
                .orderByDesc(EvaluationResult::getTotalScore);

        if ("department".equals(type)) {
            wrapper.orderByAsc(EvaluationResult::getDepartmentRank);
        } else {
            wrapper.orderByAsc(EvaluationResult::getMajorRank);
        }

        return list(wrapper);
    }

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
