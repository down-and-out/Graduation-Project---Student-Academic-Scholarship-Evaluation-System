package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ResultAppeal;
import com.scholarship.service.ResultAppealService;
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

/**
 * 结果异议控制器
 * <p>
 * 处理评定结果异议相关的请求，包括异议提交、处理等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/result-appeal")
@RequiredArgsConstructor
@Tag(name = "09-结果异议管理", description = "评定结果异议的提交和处理接口")
public class ResultAppealController {

    private final ResultAppealService resultAppealService;

    /**
     * 分页查询异议
     *
     * @param current  当前页
     * @param size     每页大小
     * @param studentId 学生 ID
     * @param appealStatus 异议状态（1-待处理 2-处理中 3-已处理）
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询异议", description = "支持按学生 ID、异议状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<ResultAppeal>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "异议状态", example = "1") @RequestParam(required = false) Integer appealStatus) {
        Page<ResultAppeal> page = new Page<>(current, size);
        IPage<ResultAppeal> result = resultAppealService.page(page);
        return Result.success(result);
    }

    /**
     * 提交异议
     *
     * @param appeal 异议信息
     * @return 是否成功
     */
    @PostMapping
    @Operation(summary = "提交异议", description = "学生对评定结果有异议时可提交申诉")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "提交成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public Result<Void> submit(@Valid @RequestBody ResultAppeal appeal) {
        boolean success = resultAppealService.submitAppeal(appeal);
        return success ? Result.success("提交成功") : Result.error("提交失败");
    }

    /**
     * 处理异议
     *
     * @param id 异议 ID
     * @param request 处理请求
     * @return 是否成功
     */
    @PutMapping("/handle/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "处理异议", description = "管理员对异议进行处理并填写处理结果")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "处理成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "异议不存在")
    })
    public Result<Void> handle(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "处理参数")
            @RequestBody HandleAppealRequest request) {
        boolean success = resultAppealService.handleAppeal(id, request.handleResult());
        return success ? Result.success("处理成功") : Result.error("处理失败");
    }

    /**
     * 处理异议请求体
     */
    public record HandleAppealRequest(
        String handleResult
    ) {}
}
