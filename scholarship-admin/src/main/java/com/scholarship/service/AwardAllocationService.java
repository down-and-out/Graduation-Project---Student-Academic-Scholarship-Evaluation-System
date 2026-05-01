package com.scholarship.service;

import com.scholarship.entity.EvaluationResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 奖项分配服务接口
 * <p>
 * 负责根据排名和规则自动分配奖项等级和奖学金金额
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface AwardAllocationService {

    /**
     * 批量分配奖项
     * <p>
     * 根据批次 ID 自动为所有评定结果分配奖项等级和金额
     * </p>
     *
     * @param batchId 批次 ID
     * @return 分配结果统计
     */
    AwardAllocationResult allocateAwards(Long batchId);

    /**
     * 基于已加载的排序结果分配奖项（避免重复 DB 查询）
     *
     * @param batchId 批次 ID
     * @param sortedResults 已按总分降序排列的评定结果列表
     * @return 分配结果统计
     */
    AwardAllocationResult allocateAwards(Long batchId, List<EvaluationResult> sortedResults);

    /**
     * 为单个评定结果分配奖项
     *
     * @param result 评定结果
     * @param quotaConfig 配额配置
     * @return 是否分配成功
     */
    boolean allocateAward(EvaluationResult result, Map<Integer, AwardQuota> quotaConfig);

    /**
     * 获取奖项等级名称
     *
     * @param awardLevel 奖项等级代码
     * @return 奖项等级名称
     */
    String getAwardLevelName(Integer awardLevel);

    /**
     * 根据排名和配额确定奖项等级
     *
     * @param rank 排名
     * @param total 总人数
     * @param quotaConfig 配额配置
     * @return 奖项等级
     */
    Integer determineAwardLevel(Integer rank, Integer total, Map<Integer, AwardQuota> quotaConfig);

    /**
     * 计算奖学金金额
     *
     * @param awardLevel 奖项等级
     * @param batchId 批次 ID
     * @return 奖学金金额
     */
    BigDecimal calculateAwardAmount(Integer awardLevel, Long batchId);

    /**
     * 奖项配额配置
     */
    class AwardQuota {
        /**
         * 奖项等级（1-特等 2-一等 3-二等 4-三等）
         */
        private Integer awardLevel;

        /**
         * 配额比例（0.0-1.0，如 0.1 表示 10%）
         */
        private Double ratio;

        /**
         * 单项金额
         */
        private BigDecimal amount;

        /**
         * 最大名额
         */
        private Integer maxCount;

        public AwardQuota() {
        }

        public AwardQuota(Integer awardLevel, Double ratio, BigDecimal amount, Integer maxCount) {
            this.awardLevel = awardLevel;
            this.ratio = ratio;
            this.amount = amount;
            this.maxCount = maxCount;
        }

        public Integer getAwardLevel() {
            return awardLevel;
        }

        public void setAwardLevel(Integer awardLevel) {
            this.awardLevel = awardLevel;
        }

        public Double getRatio() {
            return ratio;
        }

        public void setRatio(Double ratio) {
            this.ratio = ratio;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Integer getMaxCount() {
            return maxCount;
        }

        public void setMaxCount(Integer maxCount) {
            this.maxCount = maxCount;
        }
    }

    /**
     * 奖项分配结果统计
     */
    class AwardAllocationResult {
        /**
         * 特等奖人数
         */
        private int specialCount;

        /**
         * 一等奖人数
         */
        private int firstCount;

        /**
         * 二等奖人数
         */
        private int secondCount;

        /**
         * 三等奖人数
         */
        private int thirdCount;

        /**
         * 未获奖人数
         */
        private int noAwardCount;

        /**
         * 总金额
         */
        private BigDecimal totalAmount;

        public AwardAllocationResult() {
            this.totalAmount = BigDecimal.ZERO;
        }

        public int getSpecialCount() {
            return specialCount;
        }

        public void setSpecialCount(int specialCount) {
            this.specialCount = specialCount;
        }

        public int getFirstCount() {
            return firstCount;
        }

        public void setFirstCount(int firstCount) {
            this.firstCount = firstCount;
        }

        public int getSecondCount() {
            return secondCount;
        }

        public void setSecondCount(int secondCount) {
            this.secondCount = secondCount;
        }

        public int getThirdCount() {
            return thirdCount;
        }

        public void setThirdCount(int thirdCount) {
            this.thirdCount = thirdCount;
        }

        public int getNoAwardCount() {
            return noAwardCount;
        }

        public void setNoAwardCount(int noAwardCount) {
            this.noAwardCount = noAwardCount;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        @Override
        public String toString() {
            return "奖项分配结果：特等=" + specialCount + ", 一等=" + firstCount +
                    ", 二等=" + secondCount + ", 三等=" + thirdCount +
                    ", 未获奖=" + noAwardCount + ", 总金额=" + totalAmount;
        }
    }
}
