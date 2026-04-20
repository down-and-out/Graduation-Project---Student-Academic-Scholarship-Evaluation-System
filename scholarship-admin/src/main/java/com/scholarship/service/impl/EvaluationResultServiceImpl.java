package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.dto.param.EvaluationResultAdjustRequest;
import com.scholarship.dto.query.EvaluationResultQuery;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.enums.AwardLevelEnum;
import com.scholarship.enums.ResultStatusEnum;
import com.scholarship.exception.BusinessException;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.service.EvaluationBatchService;
import com.scholarship.service.EvaluationResultService;
import com.scholarship.vo.AdminEvaluationResultVO;
import com.scholarship.vo.EvaluationResultExportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评定结果服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationResultServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResult> implements EvaluationResultService {

    private final EvaluationResultMapper evaluationResultMapper;
    private final EvaluationBatchService evaluationBatchService;

    @Override
    public IPage<EvaluationResult> pageResults(Long current, Long size, Long batchId, Long studentId, Integer status, String keyword) {
        // 创建分页对象
        Page<EvaluationResult> page = new Page<>(current, size);

        // 构建查询条件
        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();

        // 批次筛选
        if (batchId != null) {
            wrapper.eq(EvaluationResult::getBatchId, batchId);
        }

        // 学生 ID 筛选
        if (studentId != null) {
            wrapper.eq(EvaluationResult::getStudentId, studentId);
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(EvaluationResult::getResultStatus, status);
        }

        // 关键词模糊查询（学号或姓名）
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(EvaluationResult::getStudentNo, keyword)
                    .or()
                    .like(EvaluationResult::getStudentName, keyword));
        }

        // 按总分降序排列
        wrapper.orderByDesc(EvaluationResult::getTotalScore);

        return evaluationResultMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<AdminEvaluationResultVO> pageAdminResults(Long current, Long size, Long batchId, Long studentId, Integer status, String keyword) {
        IPage<EvaluationResult> pageResult = pageResults(current, size, batchId, studentId, status, keyword);
        Map<Long, String> batchNameMap = loadBatchNameMap(pageResult.getRecords());

        Page<AdminEvaluationResultVO> adminPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        adminPage.setRecords(pageResult.getRecords().stream()
                .map(item -> toAdminVO(item, batchNameMap))
                .toList());
        return adminPage;
    }

    @Override
    public IPage<EvaluationResult> queryPage(EvaluationResultQuery query) {
        return pageResults(
                query.getCurrent(),
                query.getSize(),
                query.getBatchId(),
                query.getStudentId(),
                query.getResultStatus(),
                query.getKeyword()
        );
    }

    @Override
    public EvaluationResult getStudentResult(Long studentId, Long batchId) {
        log.debug("获取学生评定结果，studentId={}, batchId={}", studentId, batchId);

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationResult::getStudentId, studentId);

        if (batchId != null) {
            wrapper.eq(EvaluationResult::getBatchId, batchId);
        } else {
            // 如果没有传入 batchId，获取最新的评定结果
            wrapper.orderByDesc(EvaluationResult::getBatchId);
        }

        return getOne(wrapper);
    }

    @Override
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
        log.info("确认评定结果，id={}", id);

        EvaluationResult result = getById(id);
        if (result == null) {
            throw new BusinessException("评定结果不存在");
        }

        result.setResultStatus(ResultStatusEnum.CONFIRMED.getCode());
        return updateById(result);
    }

    @Override
    @Transactional
    public boolean objectResult(Long id) {
        log.info("标记评定结果有异议，id={}", id);

        EvaluationResult result = getById(id);
        if (result == null) {
            throw new BusinessException("评定结果不存在");
        }

        result.setResultStatus(ResultStatusEnum.OBJECTED.getCode());
        return updateById(result);
    }

    @Override
    public List<EvaluationResult> getBatchRanks(Long batchId, String type) {
        log.debug("获取批次排名列表，batchId={}, type={}", batchId, type);

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationResult::getBatchId, batchId)
            .orderByAsc(EvaluationResult::getTotalScore);

        if ("department".equals(type)) {
            wrapper.orderByAsc(EvaluationResult::getDepartmentRank);
        } else {
            wrapper.orderByAsc(EvaluationResult::getMajorRank);
        }

        return list(wrapper);
    }

    @Override
    public List<EvaluationResultExportVO> exportBatchResults(Long batchId) {
        log.info("导出批次评定结果，batchId={}", batchId);

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();
        if (batchId != null) {
            wrapper.eq(EvaluationResult::getBatchId, batchId);
        }
        wrapper.orderByDesc(EvaluationResult::getTotalScore);

        List<EvaluationResult> results = list(wrapper);

        // 转换为导出 VO
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
        return updateById(result);
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
                .collect(Collectors.toMap(EvaluationBatch::getId, EvaluationBatch::getBatchName, (left, right) -> left, HashMap::new));
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
