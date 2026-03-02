package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.service.EvaluationBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 评定批次控制器
 * <p>
 * 处理评定批次相关的请求（仅管理员可操作）
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/evaluation-batch")
@RequiredArgsConstructor
@Tag(name = "07-评定批次管理", description = "奖学金评定批次的配置接口（仅管理员）")
public class EvaluationBatchController {

    private final EvaluationBatchService evaluationBatchService;

    /**
     * 分页查询评定批次
     *
     * @param current  当前页
     * @param size     每页大小
     * @param batchStatus 批次状态（0-未开始 1-进行中 2-已结束）
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询评定批次", description = "支持按批次状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<IPage<EvaluationBatch>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "批次状态", example = "1") @RequestParam(required = false) Integer batchStatus) {
        Page<EvaluationBatch> page = new Page<>(current, size);
        IPage<EvaluationBatch> result = evaluationBatchService.page(page);
        return Result.success(result);
    }

    /**
     * 获取批次详情
     *
     * @param id 批次 ID
     * @return 批次详情
     */
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

    /**
     * 新增评定批次
     *
     * @param batch 批次信息
     * @return 是否成功
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "新增评定批次", description = "创建新的奖学金评定批次")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "新增成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> add(@Valid @RequestBody EvaluationBatch batch) {
        boolean success = evaluationBatchService.save(batch);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新评定批次
     *
     * @param batch 批次信息
     * @return 是否成功
     */
    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新评定批次", description = "修改评定批次信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Void> update(@Valid @RequestBody EvaluationBatch batch) {
        boolean success = evaluationBatchService.updateById(batch);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除评定批次
     *
     * @param id 批次 ID
     * @return 是否成功
     */
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

    /**
     * 开启批次
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    @PutMapping("/start/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "开启批次", description = "将批次状态设置为申请中")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Void> startBatch(@PathVariable Long id) {
        EvaluationBatch batch = evaluationBatchService.getById(id);
        if (batch == null) {
            return Result.error("批次不存在");
        }
        batch.setBatchStatus(2); // 申请中
        boolean success = evaluationBatchService.updateById(batch);
        return success ? Result.success("批次已开启") : Result.error("操作失败");
    }

    /**
     * 获取可用批次列表
     *
     * @return 可用批次列表
     */
    @GetMapping("/available")
    @Operation(summary = "获取可用批次列表", description = "获取当前申请中的批次列表，所有用户可访问")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<EvaluationBatch>> getAvailable() {
        List<EvaluationBatch> batches = evaluationBatchService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EvaluationBatch>()
                .eq(EvaluationBatch::getBatchStatus, 2) // 申请中
        );
        return Result.success(batches);
    }

    /**
     * 发布批次
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    @PutMapping("/publish/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "发布批次", description = "将批次状态设置为未开始（已发布）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Void> publishBatch(@PathVariable Long id) {
        EvaluationBatch batch = evaluationBatchService.getById(id);
        if (batch == null) {
            return Result.error("批次不存在");
        }
        batch.setBatchStatus(1); // 1-未开始（已发布）
        boolean success = evaluationBatchService.updateById(batch);
        return success ? Result.success("批次已发布") : Result.error("操作失败");
    }

    /**
     * 结束批次
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    @PutMapping("/close/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "结束批次", description = "将批次状态设置为已完成")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Void> closeBatch(@PathVariable Long id) {
        EvaluationBatch batch = evaluationBatchService.getById(id);
        if (batch == null) {
            return Result.error("批次不存在");
        }
        batch.setBatchStatus(5); // 5-已完成
        boolean success = evaluationBatchService.updateById(batch);
        return success ? Result.success("批次已结束") : Result.error("操作失败");
    }

    /**
     * 开始评审
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    @PutMapping("/start-review/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "开始评审", description = "将批次状态从申请中切换到评审中")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Void> startReview(@PathVariable Long id) {
        EvaluationBatch batch = evaluationBatchService.getById(id);
        if (batch == null) {
            return Result.error("批次不存在");
        }
        batch.setBatchStatus(3); // 3-评审中
        boolean success = evaluationBatchService.updateById(batch);
        return success ? Result.success("已进入评审阶段") : Result.error("操作失败");
    }

    /**
     * 开始公示
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    @PutMapping("/start-publicity/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "开始公示", description = "将批次状态从评审中切换到公示中")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Void> startPublicity(@PathVariable Long id) {
        EvaluationBatch batch = evaluationBatchService.getById(id);
        if (batch == null) {
            return Result.error("批次不存在");
        }
        batch.setBatchStatus(4); // 4-公示中
        boolean success = evaluationBatchService.updateById(batch);
        return success ? Result.success("已进入公示阶段") : Result.error("操作失败");
    }
}
