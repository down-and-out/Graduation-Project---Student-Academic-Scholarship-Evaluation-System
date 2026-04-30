package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.dto.BatchAwardConfig;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.service.AwardAllocationService;
import com.scholarship.service.EvaluationBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwardAllocationServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResult>
        implements AwardAllocationService {

    private static final double SPECIAL_RATIO = 0.05;
    private static final double FIRST_RATIO = 0.10;
    private static final double SECOND_RATIO = 0.20;
    private static final double THIRD_RATIO = 0.30;

    private static final BigDecimal SPECIAL_AMOUNT = new BigDecimal("10000");
    private static final BigDecimal FIRST_AMOUNT = new BigDecimal("5000");
    private static final BigDecimal SECOND_AMOUNT = new BigDecimal("3000");
    private static final BigDecimal THIRD_AMOUNT = new BigDecimal("1000");

    private final EvaluationBatchService evaluationBatchService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AwardAllocationResult allocateAwards(Long batchId) {
        AwardAllocationResult result = new AwardAllocationResult();
        EvaluationBatch batch = evaluationBatchService.getById(batchId);
        if (batch == null) {
            return result;
        }

        List<EvaluationResult> results = list(new LambdaQueryWrapper<EvaluationResult>()
                .eq(EvaluationResult::getBatchId, batchId)
                .orderByDesc(EvaluationResult::getTotalScore));
        if (results.isEmpty()) {
            return result;
        }

        int total = results.size();
        Map<Integer, AwardQuota> quotaConfig = buildQuotaConfig(batch, total);

        int specialQuota = quotaConfig.get(1).getMaxCount();
        int firstQuota = quotaConfig.get(2).getMaxCount();
        int secondQuota = quotaConfig.get(3).getMaxCount();
        int thirdQuota = quotaConfig.get(4).getMaxCount();

        // 按院系分组计算院内排名
        Map<Long, Integer> deptRankMap = new HashMap<>();
        Map<String, List<EvaluationResult>> deptGroups = new LinkedHashMap<>();
        for (EvaluationResult er : results) {
            String dept = er.getDepartment() != null ? er.getDepartment() : "";
            deptGroups.computeIfAbsent(dept, k -> new ArrayList<>()).add(er);
        }
        for (List<EvaluationResult> deptResults : deptGroups.values()) {
            int deptRank = 0;
            for (EvaluationResult er : deptResults) {
                deptRank++;
                deptRankMap.put(er.getId(), deptRank);
            }
        }

        int currentRank = 0;
        int specialCount = 0;
        int firstCount = 0;
        int secondCount = 0;
        int thirdCount = 0;
        int noAwardCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<EvaluationResult> updateEntities = new ArrayList<>();

        for (EvaluationResult evalResult : results) {
            currentRank++;
            Integer awardLevel;
            BigDecimal awardAmount = BigDecimal.ZERO;

            if (currentRank <= specialQuota && specialCount < specialQuota) {
                awardLevel = 1;
                awardAmount = quotaConfig.get(1).getAmount();
                specialCount++;
            } else if (currentRank <= specialQuota + firstQuota && firstCount < firstQuota) {
                awardLevel = 2;
                awardAmount = quotaConfig.get(2).getAmount();
                firstCount++;
            } else if (currentRank <= specialQuota + firstQuota + secondQuota && secondCount < secondQuota) {
                awardLevel = 3;
                awardAmount = quotaConfig.get(3).getAmount();
                secondCount++;
            } else if (currentRank <= specialQuota + firstQuota + secondQuota + thirdQuota && thirdCount < thirdQuota) {
                awardLevel = 4;
                awardAmount = quotaConfig.get(4).getAmount();
                thirdCount++;
            } else {
                awardLevel = 5;
                noAwardCount++;
            }

            EvaluationResult updateEntity = new EvaluationResult();
            updateEntity.setId(evalResult.getId());
            updateEntity.setAwardLevel(awardLevel);
            updateEntity.setAwardAmount(awardAmount);
            updateEntity.setDepartmentRank(deptRankMap.getOrDefault(evalResult.getId(), currentRank));
            updateEntities.add(updateEntity);

            if (awardLevel != 5) {
                totalAmount = totalAmount.add(awardAmount);
            }
        }

        updateBatchById(updateEntities);

        result.setSpecialCount(specialCount);
        result.setFirstCount(firstCount);
        result.setSecondCount(secondCount);
        result.setThirdCount(thirdCount);
        result.setNoAwardCount(noAwardCount);
        result.setTotalAmount(totalAmount);

        EvaluationBatch batchUpdate = new EvaluationBatch();
        batchUpdate.setId(batchId);
        batchUpdate.setWinnerCount(specialCount + firstCount + secondCount + thirdCount);
        batchUpdate.setTotalAmount(totalAmount);
        evaluationBatchService.updateById(batchUpdate);

        return result;
    }

    @Override
    public boolean allocateAward(EvaluationResult result, Map<Integer, AwardQuota> quotaConfig) {
        if (result.getDepartmentRank() == null) {
            return false;
        }

        int total = (int) count(new LambdaQueryWrapper<EvaluationResult>()
                .eq(EvaluationResult::getBatchId, result.getBatchId()));

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
        return switch (awardLevel) {
            case 1 -> "特等奖学金";
            case 2 -> "一等奖学金";
            case 3 -> "二等奖学金";
            case 4 -> "三等奖学金";
            case 5 -> "未获奖";
            default -> "未知";
        };
    }

    @Override
    public Integer determineAwardLevel(Integer rank, Integer total, Map<Integer, AwardQuota> quotaConfig) {
        if (rank == null || total == null || total == 0) {
            return 5;
        }

        Map<Integer, AwardQuota> safeQuotaConfig = quotaConfig == null || quotaConfig.isEmpty()
                ? buildDefaultQuotaConfig(total)
                : quotaConfig;

        int specialMax = safeQuotaConfig.getOrDefault(1, defaultQuota(1, total)).getMaxCount();
        int firstMax = safeQuotaConfig.getOrDefault(2, defaultQuota(2, total)).getMaxCount();
        int secondMax = safeQuotaConfig.getOrDefault(3, defaultQuota(3, total)).getMaxCount();
        int thirdMax = safeQuotaConfig.getOrDefault(4, defaultQuota(4, total)).getMaxCount();

        if (rank <= specialMax) {
            return 1;
        }
        if (rank <= specialMax + firstMax) {
            return 2;
        }
        if (rank <= specialMax + firstMax + secondMax) {
            return 3;
        }
        if (rank <= specialMax + firstMax + secondMax + thirdMax) {
            return 4;
        }
        return 5;
    }

    @Override
    public BigDecimal calculateAwardAmount(Integer awardLevel, Long batchId) {
        if (awardLevel == null) {
            return BigDecimal.ZERO;
        }

        EvaluationBatch batch = evaluationBatchService.getById(batchId);
        if (batch != null) {
            for (BatchAwardConfig config : batch.getAwardConfigs()) {
                if (config.getAwardLevel() != null && config.getAwardLevel().equals(awardLevel)) {
                    return config.getAmount();
                }
            }
        }

        return switch (awardLevel) {
            case 1 -> SPECIAL_AMOUNT;
            case 2 -> FIRST_AMOUNT;
            case 3 -> SECOND_AMOUNT;
            case 4 -> THIRD_AMOUNT;
            default -> BigDecimal.ZERO;
        };
    }

    private Map<Integer, AwardQuota> buildQuotaConfig(EvaluationBatch batch, int total) {
        List<BatchAwardConfig> configs = batch.getAwardConfigs();
        if (configs == null || configs.isEmpty()) {
            return buildDefaultQuotaConfig(total);
        }

        Map<Integer, AwardQuota> quotaConfig = new HashMap<>();
        for (BatchAwardConfig config : configs) {
            if (config.getAwardLevel() == null || config.getRatio() == null || config.getAmount() == null) {
                continue;
            }
            double ratio = config.getRatio().divide(new BigDecimal("100")).doubleValue();
            quotaConfig.put(config.getAwardLevel(),
                    new AwardQuota(
                            config.getAwardLevel(),
                            ratio,
                            config.getAmount(),
                            (int) Math.ceil(total * ratio)
                    ));
        }
        for (int level = 1; level <= 4; level++) {
            quotaConfig.putIfAbsent(level, defaultQuota(level, total));
        }
        return quotaConfig;
    }

    private Map<Integer, AwardQuota> buildDefaultQuotaConfig(int total) {
        Map<Integer, AwardQuota> quotaConfig = new HashMap<>();
        for (int level = 1; level <= 4; level++) {
            quotaConfig.put(level, defaultQuota(level, total));
        }
        return quotaConfig;
    }

    private AwardQuota defaultQuota(int level, int total) {
        return switch (level) {
            case 1 -> new AwardQuota(1, SPECIAL_RATIO, SPECIAL_AMOUNT, (int) Math.ceil(total * SPECIAL_RATIO));
            case 2 -> new AwardQuota(2, FIRST_RATIO, FIRST_AMOUNT, (int) Math.ceil(total * FIRST_RATIO));
            case 3 -> new AwardQuota(3, SECOND_RATIO, SECOND_AMOUNT, (int) Math.ceil(total * SECOND_RATIO));
            case 4 -> new AwardQuota(4, THIRD_RATIO, THIRD_AMOUNT, (int) Math.ceil(total * THIRD_RATIO));
            default -> new AwardQuota(level, 0D, BigDecimal.ZERO, 0);
        };
    }
}
