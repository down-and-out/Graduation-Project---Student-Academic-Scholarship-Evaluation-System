package com.scholarship.entity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.scholarship.dto.BatchAwardConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评定批次实体类
 * <p>
 * 对应数据库表：evaluation_batch
 * 存储奖学金评定批次信息，如2024年秋季学期奖学金评定
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("evaluation_batch")
@Schema(description = "评定批次")
public class EvaluationBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "批次ID")
    private Long id;

    /**
     * 批次名称
     */
    @Schema(description = "批次名称")
    @TableField("batch_name")
    @JsonProperty("name")
    private String batchName;

    /**
     * 批次编码
     */
    @Schema(description = "批次编码")
    private String batchCode;

    /**
     * 学年
     */
    @Schema(description = "学年")
    private String academicYear;

    /**
     * 学期
     * 1-第一学期 2-第二学期 3-全年
     */
    @Schema(description = "学期：1-第一学期 2-第二学期 3-全年")
    private Integer semester;

    /**
     * 申请开始日期
     */
    @Schema(description = "申请开始日期")
    @TableField("application_start_date")
    @JsonProperty("startDate")
    private LocalDate applicationStartDate;

    /**
     * 申请结束日期
     */
    @Schema(description = "申请结束日期")
    @TableField("application_end_date")
    @JsonProperty("endDate")
    private LocalDate applicationEndDate;

    /**
     * 评审开始日期
     */
    @Schema(description = "评审开始日期")
    private LocalDate reviewStartDate;

    /**
     * 评审结束日期
     */
    @Schema(description = "评审结束日期")
    private LocalDate reviewEndDate;

    /**
     * 公示开始日期
     */
    @Schema(description = "公示开始日期")
    private LocalDate publicityStartDate;

    /**
     * 公示结束日期
     */
    @Schema(description = "公示结束日期")
    private LocalDate publicityEndDate;

    /**
     * 批次状态
     * 1-未开始 2-申请中 3-评审中 4-公示中 5-已完成
     */
    @Schema(description = "批次状态：1-未开始 2-申请中 3-评审中 4-公示中 5-已完成")
    @TableField("batch_status")
    @JsonProperty("status")
    private Integer batchStatus;

    /**
     * 奖学金总额
     */
    @Schema(description = "奖学金总额")
    @TableField("total_amount")
    @DecimalMin(value = "0", message = "奖学金总额不能为负数")
    private BigDecimal totalAmount;

    /**
     * 获奖人数
     */
    @Schema(description = "获奖人数")
    @TableField("winner_count")
    private Integer winnerCount;

    /**
     * 评定说明
     */
    @Schema(description = "评定说明")
    private String description;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 逻辑删除标记
     * 0-未删除 1-已删除
     */
    @TableLogic
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonProperty("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonProperty("updateTime")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    @JsonIgnore
    @TableField("award_configs")
    private String awardConfigsJson;

    @JsonIgnore
    @TableField("selected_rule_ids")
    private String selectedRuleIdsJson;

    @TableField(exist = false)
    @JsonProperty("awardConfigs")
    @Schema(description = "批次奖项配置")
    private List<BatchAwardConfig> awardConfigs;

    @TableField(exist = false)
    @JsonProperty("selectedRuleIds")
    @Schema(description = "批次参与评定的规则ID列表")
    private List<Long> selectedRuleIds;

    public List<BatchAwardConfig> getAwardConfigs() {
        if (awardConfigs != null) {
            return awardConfigs;
        }
        if (awardConfigsJson == null || awardConfigsJson.isBlank()) {
            awardConfigs = defaultAwardConfigs();
            return awardConfigs;
        }
        try {
            awardConfigs = JSON.parseObject(awardConfigsJson, new TypeReference<List<BatchAwardConfig>>() {
            });
        } catch (Exception ignored) {
            awardConfigs = defaultAwardConfigs();
        }
        if (awardConfigs == null || awardConfigs.isEmpty()) {
            awardConfigs = defaultAwardConfigs();
        }
        awardConfigs = normalizeAwardConfigs(awardConfigs);
        return awardConfigs;
    }

    public void setAwardConfigs(List<BatchAwardConfig> awardConfigs) {
        this.awardConfigs = normalizeAwardConfigs(awardConfigs);
        this.awardConfigsJson = this.awardConfigs == null || this.awardConfigs.isEmpty()
                ? null
                : JSON.toJSONString(this.awardConfigs);
    }

    public void setAwardConfigsJson(String awardConfigsJson) {
        this.awardConfigsJson = awardConfigsJson;
        this.awardConfigs = null;
    }

    public List<Long> getSelectedRuleIds() {
        if (selectedRuleIds != null) {
            return selectedRuleIds;
        }
        if (selectedRuleIdsJson == null || selectedRuleIdsJson.isBlank()) {
            selectedRuleIds = new ArrayList<>();
            return selectedRuleIds;
        }
        selectedRuleIds = parseRuleIds(selectedRuleIdsJson);
        return selectedRuleIds;
    }

    public void setSelectedRuleIds(List<Long> selectedRuleIds) {
        this.selectedRuleIds = normalizeRuleIds(selectedRuleIds);
        this.selectedRuleIdsJson = this.selectedRuleIds.isEmpty()
                ? ""
                : this.selectedRuleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public void setSelectedRuleIdsJson(String selectedRuleIdsJson) {
        this.selectedRuleIdsJson = selectedRuleIdsJson;
        this.selectedRuleIds = null;
    }

    private List<BatchAwardConfig> normalizeAwardConfigs(List<BatchAwardConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return defaultAwardConfigs();
        }
        return configs.stream()
                .filter(item -> item != null && item.getAwardLevel() != null)
                .sorted(Comparator.comparing(BatchAwardConfig::getAwardLevel))
                .collect(Collectors.toList());
    }

    private List<Long> normalizeRuleIds(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return ruleIds.stream()
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> parseRuleIds(String rawIds) {
        List<Long> ruleIds = new ArrayList<>();
        for (String value : rawIds.split(",")) {
            if (!value.isBlank()) {
                ruleIds.add(Long.parseLong(value.trim()));
            }
        }
        return normalizeRuleIds(ruleIds);
    }

    private List<BatchAwardConfig> defaultAwardConfigs() {
        List<BatchAwardConfig> defaults = new ArrayList<>();
        defaults.add(buildDefaultAwardConfig(1, "5", "10000"));
        defaults.add(buildDefaultAwardConfig(2, "10", "5000"));
        defaults.add(buildDefaultAwardConfig(3, "20", "3000"));
        defaults.add(buildDefaultAwardConfig(4, "30", "1000"));
        return defaults;
    }

    private BatchAwardConfig buildDefaultAwardConfig(Integer awardLevel, String ratio, String amount) {
        BatchAwardConfig config = new BatchAwardConfig();
        config.setAwardLevel(awardLevel);
        config.setRatio(new BigDecimal(ratio));
        config.setAmount(new BigDecimal(amount));
        return config;
    }
}
