package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.service.EvaluationRankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 排名生成服务实现类
 * <p>
 * 负责生成学生的各类排名，包括院系排名、专业排名等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class EvaluationRankServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResult>
        implements EvaluationRankService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, EvaluationResult> generateBatchRanks(Long batchId) {
        log.info("开始生成批次排名，batchId={}", batchId);

        // 查询该批次下所有评定结果
        List<EvaluationResult> results = list(
            new LambdaQueryWrapper<EvaluationResult>()
                .eq(EvaluationResult::getBatchId, batchId)
                .orderByDesc(EvaluationResult::getTotalScore)
        );

        if (results.isEmpty()) {
            log.warn("该批次下暂无评定结果，batchId={}", batchId);
            return new HashMap<>();
        }

        // 按院系统分组
        Map<String, List<EvaluationResult>> departmentMap = results.stream()
            .filter(r -> r.getDepartment() != null)
            .collect(Collectors.groupingBy(EvaluationResult::getDepartment));

        // 按专业分组
        Map<String, List<EvaluationResult>> majorMap = results.stream()
            .filter(r -> r.getMajor() != null)
            .collect(Collectors.groupingBy(EvaluationResult::getMajor));

        // 计算各院系内部排名
        for (Map.Entry<String, List<EvaluationResult>> entry : departmentMap.entrySet()) {
            List<EvaluationResult> deptResults = entry.getValue();
            // 按总分排序
            deptResults.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
            // 设置排名
            for (int i = 0; i < deptResults.size(); i++) {
                deptResults.get(i).setDepartmentRank(i + 1);
            }
        }

        // 计算各专业内部排名
        for (Map.Entry<String, List<EvaluationResult>> entry : majorMap.entrySet()) {
            List<EvaluationResult> majorResults = entry.getValue();
            // 按总分排序
            majorResults.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
            // 设置排名
            for (int i = 0; i < majorResults.size(); i++) {
                majorResults.get(i).setMajorRank(i + 1);
            }
        }

        // 批量更新排名
        updateRanksBatch(batchId, results);

        // 构建返回结果
        Map<Long, EvaluationResult> resultMap = new HashMap<>();
        for (EvaluationResult result : results) {
            resultMap.put(result.getStudentId(), result);
        }

        log.info("批次排名生成完成，batchId={}, 学生数量={}", batchId, resultMap.size());
        return resultMap;
    }

    @Override
    public List<EvaluationResult> calculateDepartmentRank(Long batchId, String department) {
        log.debug("计算院系排名，batchId={}, department={}", batchId, department);

        List<EvaluationResult> results = list(
            new LambdaQueryWrapper<EvaluationResult>()
                .eq(EvaluationResult::getBatchId, batchId)
                .eq(EvaluationResult::getDepartment, department)
                .orderByDesc(EvaluationResult::getTotalScore)
        );

        // 设置排名
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setDepartmentRank(i + 1);
        }

        return results;
    }

    @Override
    public List<EvaluationResult> calculateMajorRank(Long batchId, String major) {
        log.debug("计算专业排名，batchId={}, major={}", batchId, major);

        List<EvaluationResult> results = list(
            new LambdaQueryWrapper<EvaluationResult>()
                .eq(EvaluationResult::getBatchId, batchId)
                .eq(EvaluationResult::getMajor, major)
                .orderByDesc(EvaluationResult::getTotalScore)
        );

        // 设置排名
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setMajorRank(i + 1);
        }

        return results;
    }

    @Override
    public Integer getDepartmentRank(Long batchId, Long studentId) {
        EvaluationResult result = getOne(
            new LambdaQueryWrapper<EvaluationResult>()
                .eq(EvaluationResult::getBatchId, batchId)
                .eq(EvaluationResult::getStudentId, studentId)
        );
        return result != null ? result.getDepartmentRank() : null;
    }

    @Override
    public Integer getMajorRank(Long batchId, Long studentId) {
        EvaluationResult result = getOne(
            new LambdaQueryWrapper<EvaluationResult>()
                .eq(EvaluationResult::getBatchId, batchId)
                .eq(EvaluationResult::getStudentId, studentId)
        );
        return result != null ? result.getMajorRank() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRanksBatch(Long batchId, List<EvaluationResult> results) {
        log.info("批量更新排名，batchId={}, 数量={}", batchId, results.size());

        int count = 0;
        for (EvaluationResult result : results) {
            // 只更新排名字段
            EvaluationResult updateEntity = new EvaluationResult();
            updateEntity.setId(result.getId());
            updateEntity.setDepartmentRank(result.getDepartmentRank());
            updateEntity.setMajorRank(result.getMajorRank());

            if (updateById(updateEntity)) {
                count++;
            }
        }

        log.info("批量更新排名完成，batchId={}, 成功数量={}", batchId, count);
        return count;
    }
}
