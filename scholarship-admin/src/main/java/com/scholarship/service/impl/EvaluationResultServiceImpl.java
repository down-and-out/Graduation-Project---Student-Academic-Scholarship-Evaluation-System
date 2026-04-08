package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.service.EvaluationResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
}
