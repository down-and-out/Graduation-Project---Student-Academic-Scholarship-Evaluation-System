package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.service.AwardAllocationService;
import com.scholarship.service.EvaluationBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 奖项分配服务实现类
 * <p>
 * 负责根据排名和规则自动分配奖项等级和奖学金金额
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class AwardAllocationServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResult>
        implements AwardAllocationService {

    private final EvaluationBatchService evaluationBatchService;

    public AwardAllocationServiceImpl(EvaluationBatchService evaluationBatchService) {
        this.evaluationBatchService = evaluationBatchService;
    }

    // 默认配额配置（可按需调整）
    private static final double SPECIAL_RATIO = 0.05;  // 特等奖 5%
    private static final double FIRST_RATIO = 0.10;    // 一等奖 10%
    private static final double SECOND_RATIO = 0.20;   // 二等奖 20%
    private static final double THIRD_RATIO = 0.30;    // 三等奖 30%

    // 默认金额（可通过批次配置覆盖）
    private static final BigDecimal SPECIAL_AMOUNT = new BigDecimal("10000");
    private static final BigDecimal FIRST_AMOUNT = new BigDecimal("5000");
    private static final BigDecimal SECOND_AMOUNT = new BigDecimal("3000");
    private static final BigDecimal THIRD_AMOUNT = new BigDecimal("1000");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AwardAllocationResult allocateAwards(Long batchId) {
        log.info("开始批量分配奖项，batchId={}", batchId);

        AwardAllocationResult result = new AwardAllocationResult();

        // 查询批次信息
        EvaluationBatch batch = evaluationBatchService.getById(batchId);
        if (batch == null) {
            log.error("批次不存在，batchId={}", batchId);
            return result;
        }

        // 查询该批次下所有评定结果（按总分排序）
        List<EvaluationResult> results = list(
            new LambdaQueryWrapper<EvaluationResult>()
                .eq(EvaluationResult::getBatchId, batchId)
                .orderByDesc(EvaluationResult::getTotalScore)
        );

        if (results.isEmpty()) {
            log.warn("该批次下暂无评定结果，batchId={}", batchId);
            return result;
        }

        int total = results.size();
        log.info("该批次共有 {} 名学生参与评定", total);

        // 构建默认配额配置
        Map<Integer, AwardQuota> quotaConfig = new HashMap<>();
        quotaConfig.put(1, new AwardQuota(1, SPECIAL_RATIO, SPECIAL_AMOUNT, (int) Math.ceil(total * SPECIAL_RATIO)));
        quotaConfig.put(2, new AwardQuota(2, FIRST_RATIO, FIRST_AMOUNT, (int) Math.ceil(total * FIRST_RATIO)));
        quotaConfig.put(3, new AwardQuota(3, SECOND_RATIO, SECOND_AMOUNT, (int) Math.ceil(total * SECOND_RATIO)));
        quotaConfig.put(4, new AwardQuota(4, THIRD_RATIO, THIRD_AMOUNT, (int) Math.ceil(total * THIRD_RATIO)));

        // 计算各奖项名额
        int specialQuota = quotaConfig.get(1).getMaxCount();
        int firstQuota = quotaConfig.get(2).getMaxCount();
        int secondQuota = quotaConfig.get(3).getMaxCount();
        int thirdQuota = quotaConfig.get(4).getMaxCount();

        int currentRank = 0;
        int specialCount = 0, firstCount = 0, secondCount = 0, thirdCount = 0, noAwardCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (EvaluationResult evalResult : results) {
            currentRank++;
            Integer awardLevel = null;
            BigDecimal awardAmount = BigDecimal.ZERO;

            // 根据排名分配奖项
            if (currentRank <= specialQuota && specialCount < specialQuota) {
                awardLevel = 1;  // 特等
                awardAmount = SPECIAL_AMOUNT;
                specialCount++;
            } else if (currentRank <= specialQuota + firstQuota && firstCount < firstQuota) {
                awardLevel = 2;  // 一等
                awardAmount = FIRST_AMOUNT;
                firstCount++;
            } else if (currentRank <= specialQuota + firstQuota + secondQuota && secondCount < secondQuota) {
                awardLevel = 3;  // 二等
                awardAmount = SECOND_AMOUNT;
                secondCount++;
            } else if (currentRank <= specialQuota + firstQuota + secondQuota + thirdQuota && thirdCount < thirdQuota) {
                awardLevel = 4;  // 三等
                awardAmount = THIRD_AMOUNT;
                thirdCount++;
            } else {
                awardLevel = 5;  // 未获奖
                noAwardCount++;
            }

            // 更新评定结果
            EvaluationResult updateEntity = new EvaluationResult();
            updateEntity.setId(evalResult.getId());
            updateEntity.setAwardLevel(awardLevel);
            updateEntity.setAwardAmount(awardAmount);
            updateEntity.setDepartmentRank(currentRank);  // 同时更新排名

            if (updateById(updateEntity)) {
                if (awardLevel != 5) {
                    totalAmount = totalAmount.add(awardAmount);
                }
            }
        }

        // 填充统计结果
        result.setSpecialCount(specialCount);
        result.setFirstCount(firstCount);
        result.setSecondCount(secondCount);
        result.setThirdCount(thirdCount);
        result.setNoAwardCount(noAwardCount);
        result.setTotalAmount(totalAmount);

        // 更新批次的获奖人数和总金额
        if (batch != null) {
            EvaluationBatch batchUpdate = new EvaluationBatch();
            batchUpdate.setId(batchId);
            batchUpdate.setWinnerCount(specialCount + firstCount + secondCount + thirdCount);
            batchUpdate.setTotalAmount(totalAmount);
            evaluationBatchService.updateById(batchUpdate);
        }

        log.info("奖项分配完成，{}", result);
        return result;
    }

    @Override
    public boolean allocateAward(EvaluationResult result, Map<Integer, AwardQuota> quotaConfig) {
        log.debug("为单个评定结果分配奖项，studentId={}", result.getStudentId());

        if (result.getDepartmentRank() == null) {
            log.warn("排名未设置，无法分配奖项");
            return false;
        }

        // 获取总人数（从同批次结果数量推算）
        long totalLong = count(new LambdaQueryWrapper<EvaluationResult>()
            .eq(EvaluationResult::getBatchId, result.getBatchId()));
        int total = (int) totalLong;

        Integer awardLevel = determineAwardLevel(result.getDepartmentRank(), total, quotaConfig);
        BigDecimal awardAmount = calculateAwardAmount(awardLevel, result.getBatchId());

        result.setAwardLevel(awardLevel);
        result.setAwardAmount(awardAmount);

        return updateById(result);
    }

    @Override
    public String getAwardLevelName(Integer awardLevel) {
        if (awardLevel == null) {
            return "未知";
        }
        switch (awardLevel) {
            case 1: return "特等奖学金";
            case 2: return "一等奖学金";
            case 3: return "二等奖学金";
            case 4: return "三等奖学金";
            case 5: return "未获奖";
            default: return "未知";
        }
    }

    @Override
    public Integer determineAwardLevel(Integer rank, Integer total, Map<Integer, AwardQuota> quotaConfig) {
        if (rank == null || total == null || total == 0) {
            return 5;  // 未获奖
        }

        if (quotaConfig == null || quotaConfig.isEmpty()) {
            // 使用默认配置
            double specialQuota = Math.ceil(total * SPECIAL_RATIO);
            double firstQuota = Math.ceil(total * FIRST_RATIO);
            double secondQuota = Math.ceil(total * SECOND_RATIO);
            double thirdQuota = Math.ceil(total * THIRD_RATIO);

            if (rank <= specialQuota) return 1;
            if (rank <= specialQuota + firstQuota) return 2;
            if (rank <= specialQuota + firstQuota + secondQuota) return 3;
            if (rank <= specialQuota + firstQuota + secondQuota + thirdQuota) return 4;
            return 5;
        }

        // 使用自定义配额配置
        AwardQuota special = quotaConfig.get(1);
        AwardQuota first = quotaConfig.get(2);
        AwardQuota second = quotaConfig.get(3);
        AwardQuota third = quotaConfig.get(4);

        int specialMax = special != null ? special.getMaxCount() : (int) Math.ceil(total * SPECIAL_RATIO);
        int firstMax = first != null ? first.getMaxCount() : (int) Math.ceil(total * FIRST_RATIO);
        int secondMax = second != null ? second.getMaxCount() : (int) Math.ceil(total * SECOND_RATIO);
        int thirdMax = third != null ? third.getMaxCount() : (int) Math.ceil(total * THIRD_RATIO);

        if (rank <= specialMax) return 1;
        if (rank <= specialMax + firstMax) return 2;
        if (rank <= specialMax + firstMax + secondMax) return 3;
        if (rank <= specialMax + firstMax + secondMax + thirdMax) return 4;
        return 5;
    }

    @Override
    public BigDecimal calculateAwardAmount(Integer awardLevel, Long batchId) {
        if (awardLevel == null) {
            return BigDecimal.ZERO;
        }

        // 首先尝试从批次配置中获取金额
        EvaluationBatch batch = evaluationBatchService.getById(batchId);
        if (batch != null && batch.getTotalAmount() != null) {
            // 如果批次有总金额配置，按比例分配
            BigDecimal totalAmount = batch.getTotalAmount();
            int winnerCount = batch.getWinnerCount() != null ? batch.getWinnerCount() : 1;

            switch (awardLevel) {
                case 1: return totalAmount.multiply(new BigDecimal("0.4")).divide(BigDecimal.valueOf(winnerCount), 2, RoundingMode.HALF_UP);
                case 2: return totalAmount.multiply(new BigDecimal("0.3")).divide(BigDecimal.valueOf(winnerCount), 2, RoundingMode.HALF_UP);
                case 3: return totalAmount.multiply(new BigDecimal("0.2")).divide(BigDecimal.valueOf(winnerCount), 2, RoundingMode.HALF_UP);
                case 4: return totalAmount.multiply(new BigDecimal("0.1")).divide(BigDecimal.valueOf(winnerCount), 2, RoundingMode.HALF_UP);
            }
        }

        // 返回默认金额
        switch (awardLevel) {
            case 1: return SPECIAL_AMOUNT;
            case 2: return FIRST_AMOUNT;
            case 3: return SECOND_AMOUNT;
            case 4: return THIRD_AMOUNT;
            default: return BigDecimal.ZERO;
        }
    }
}
