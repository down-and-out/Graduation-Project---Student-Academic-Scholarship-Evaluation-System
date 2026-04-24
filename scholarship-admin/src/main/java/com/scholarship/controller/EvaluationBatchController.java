package com.scholarship.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.dto.BatchAwardConfig;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.exception.BusinessException;
import com.scholarship.service.EvaluationBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/evaluation-batch")
@RequiredArgsConstructor
@Tag(name = "07-评定批次管理", description = "奖学金评定批次的配置接口（仅管理员）")
public class EvaluationBatchController {

    private final EvaluationBatchService evaluationBatchService;

    @GetMapping("/page")
    @Operation(summary = "分页查询评定批次", description = "支持按学年、学期、状态筛选，并兼容旧的学年-学期组合筛选入参")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<IPage<EvaluationBatch>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "标准学年筛选，支持多个值", example = "2025,2024") @RequestParam(required = false) List<String> academicYears,
            @Parameter(description = "标准学期筛选，支持多个值", example = "1,2") @RequestParam(required = false) List<String> semesters,
            @Parameter(description = "标准状态筛选，支持多个值", example = "1,2") @RequestParam(required = false) List<String> statuses,
            @Parameter(description = "兼容旧参数：学年-学期组合值", example = "2025-1,2024-2") @RequestParam(required = false, name = "semester") List<String> legacySemester,
            @Parameter(description = "兼容旧参数：状态", example = "1,2") @RequestParam(required = false, name = "status") List<String> legacyStatus,
            @Parameter(description = "兼容旧参数：批次状态", example = "1,2") @RequestParam(required = false) List<String> batchStatus) {
        Page<EvaluationBatch> page = new Page<>(current, size);
        LambdaQueryWrapper<EvaluationBatch> wrapper = new LambdaQueryWrapper<>();

        List<String> normalizedAcademicYears = parseStringParams(academicYears);
        List<Integer> normalizedSemesters = parseIntegerParams(semesters);
        List<Integer> normalizedStatuses = parseIntegerParams(mergeRawValues(statuses, legacyStatus, batchStatus));
        List<LegacySemesterPair> legacySemesterPairs = parseLegacySemesterPairs(parseStringParams(legacySemester));

        if (!normalizedAcademicYears.isEmpty()) {
            if (normalizedAcademicYears.size() == 1) {
                wrapper.eq(EvaluationBatch::getAcademicYear, normalizedAcademicYears.get(0));
            } else {
                wrapper.in(EvaluationBatch::getAcademicYear, normalizedAcademicYears);
            }
        }

        if (!normalizedSemesters.isEmpty()) {
            if (normalizedSemesters.size() == 1) {
                wrapper.eq(EvaluationBatch::getSemester, normalizedSemesters.get(0));
            } else {
                wrapper.in(EvaluationBatch::getSemester, normalizedSemesters);
            }
        }

        if (!normalizedStatuses.isEmpty()) {
            if (normalizedStatuses.size() == 1) {
                wrapper.eq(EvaluationBatch::getBatchStatus, normalizedStatuses.get(0));
            } else {
                wrapper.in(EvaluationBatch::getBatchStatus, normalizedStatuses);
            }
        }

        if (!legacySemesterPairs.isEmpty()) {
            wrapper.and(group -> {
                boolean first = true;
                for (LegacySemesterPair pair : legacySemesterPairs) {
                    if (first) {
                        group.and(item -> item.eq(EvaluationBatch::getAcademicYear, pair.academicYear())
                                .eq(EvaluationBatch::getSemester, pair.semester()));
                        first = false;
                    } else {
                        group.or(item -> item.eq(EvaluationBatch::getAcademicYear, pair.academicYear())
                                .eq(EvaluationBatch::getSemester, pair.semester()));
                    }
                }
            });
        }

        wrapper.orderByDesc(EvaluationBatch::getCreateTime);
        IPage<EvaluationBatch> result = evaluationBatchService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取批次详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<EvaluationBatch> getById(@PathVariable Long id) {
        EvaluationBatch batch = evaluationBatchService.getById(id);
        if (batch == null) {
            return Result.error("批次不存在");
        }
        return Result.success(batch);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "新增评定批次", description = "创建新的奖学金评定批次")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "新增成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> add(@Valid @RequestBody EvaluationBatch batch) {
        validateBatchConfig(batch);
        boolean success = evaluationBatchService.save(batch);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新评定批次", description = "修改评定批次信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Void> update(@Valid @RequestBody EvaluationBatch batch) {
        EvaluationBatch existingBatch = evaluationBatchService.getById(batch.getId());
        if (existingBatch == null) {
            return Result.error("批次不存在");
        }
        mergeBatchConfigForUpdate(existingBatch, batch);
        validateBatchConfig(batch);
        boolean success = evaluationBatchService.updateById(batch);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除评定批次", description = "仅管理员可操作")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = evaluationBatchService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/start/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "开始申请", description = "将批次从未开始流转为申请中")
    public Result<Void> startBatch(@PathVariable Long id) {
        boolean success = evaluationBatchService.startBatch(id);
        return success ? Result.success("已进入申请阶段") : Result.error("操作失败");
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用批次列表", description = "获取当前申请中的批次列表，所有用户可访问")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<EvaluationBatch>> getAvailable() {
        List<EvaluationBatch> batches = evaluationBatchService.listAvailableForApplication();
        return Result.success(batches);
    }

    @PutMapping("/publish/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "发布批次", description = "校验批次处于未开始状态，兼容旧入口")
    public Result<Void> publishBatch(@PathVariable Long id) {
        boolean success = evaluationBatchService.publishBatch(id);
        return success ? Result.success("批次已发布") : Result.error("操作失败");
    }

    @PutMapping("/close/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "完成评定", description = "将批次从公示中流转为已完成")
    public Result<Void> closeBatch(@PathVariable Long id) {
        boolean success = evaluationBatchService.closeBatch(id);
        return success ? Result.success("评定已完成") : Result.error("操作失败");
    }

    @PutMapping("/start-review/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "开始评审", description = "将批次从申请中流转为评审中")
    public Result<Void> startReview(@PathVariable Long id) {
        boolean success = evaluationBatchService.startReview(id);
        return success ? Result.success("已进入评审阶段") : Result.error("操作失败");
    }

    @PutMapping("/start-publicity/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "开始公示", description = "将批次从评审中流转为公示中")
    public Result<Void> startPublicity(@PathVariable Long id) {
        boolean success = evaluationBatchService.startPublicity(id);
        return success ? Result.success("已进入公示阶段") : Result.error("操作失败");
    }

    @SafeVarargs
    private List<String> mergeRawValues(List<String>... groups) {
        List<String> merged = new ArrayList<>();
        for (List<String> group : groups) {
            if (group != null) {
                merged.addAll(group);
            }
        }
        return merged;
    }

    private List<LegacySemesterPair> parseLegacySemesterPairs(List<String> legacyValues) {
        if (legacyValues == null || legacyValues.isEmpty()) {
            return List.of();
        }

        List<LegacySemesterPair> result = new ArrayList<>();
        Set<String> uniqueKeys = new LinkedHashSet<>();
        for (String item : legacyValues) {
            String[] parts = item.split("-");
            if (parts.length != 2) {
                continue;
            }

            String academicYear = parts[0].trim();
            String semester = parts[1].trim();
            if (!academicYear.isEmpty() && !semester.isEmpty()) {
                String key = academicYear + "-" + semester;
                if (uniqueKeys.add(key)) {
                    result.add(new LegacySemesterPair(academicYear, Integer.parseInt(semester)));
                }
            }
        }
        return result;
    }

    private List<String> parseStringParams(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> result = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            for (String value : rawValue.split(",")) {
                if (!value.isBlank()) {
                    result.add(value.trim());
                }
            }
        }
        return new ArrayList<>(result);
    }

    private List<Integer> parseIntegerParams(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> result = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            for (String value : rawValue.split(",")) {
                if (!value.isBlank()) {
                    result.add(Integer.parseInt(value.trim()));
                }
            }
        }
        return new ArrayList<>(result);
    }

    private void validateBatchConfig(EvaluationBatch batch) {
        List<BatchAwardConfig> awardConfigs = batch.getAwardConfigs();
        if (awardConfigs == null || awardConfigs.isEmpty()) {
            throw new BusinessException("请配置四档奖项信息");
        }
        if (awardConfigs.size() != 4) {
            throw new BusinessException("奖项配置必须固定为四档");
        }

        Map<Integer, BatchAwardConfig> configByLevel = awardConfigs.stream()
                .collect(Collectors.toMap(BatchAwardConfig::getAwardLevel, Function.identity(), (left, right) -> right));
        for (int level = 1; level <= 4; level++) {
            BatchAwardConfig config = configByLevel.get(level);
            if (config == null) {
                throw new BusinessException("奖项配置缺少第 " + level + " 档");
            }
            if (config.getRatio() == null || config.getRatio().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("奖项比例必须大于 0");
            }
            if (config.getAmount() == null || config.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("奖项金额不能为负数");
            }
        }

        BigDecimal totalRatio = awardConfigs.stream()
                .map(BatchAwardConfig::getRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalRatio.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("奖项比例总和不能超过 100");
        }
    }

    private void mergeBatchConfigForUpdate(EvaluationBatch existingBatch, EvaluationBatch incomingBatch) {
        if (incomingBatch.getAwardConfigsJson() == null) {
            incomingBatch.setAwardConfigs(existingBatch.getAwardConfigs());
        }
        if (incomingBatch.getSelectedRuleIdsJson() == null) {
            if (existingBatch.getSelectedRuleIdsJson() == null) {
                incomingBatch.setSelectedRuleIdsJson(null);
            } else {
                incomingBatch.setSelectedRuleIds(existingBatch.getSelectedRuleIds());
            }
        }
    }

    private record LegacySemesterPair(String academicYear, Integer semester) {
    }
}
