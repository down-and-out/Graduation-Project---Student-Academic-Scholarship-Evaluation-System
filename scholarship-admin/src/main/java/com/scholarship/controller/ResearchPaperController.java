package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchPaperService;
import com.scholarship.vo.ResearchPaperVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 科研论文控制器
 * <p>
 * 处理科研论文相关的请求，包括论文提交、审核等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/paper")
@RequiredArgsConstructor
@Tag(name = "03-科研论文管理", description = "科研论文的提交、查询、审核接口")
public class ResearchPaperController {

    private final ResearchPaperService researchPaperService;

    /**
     * 分页查询论文
     *
     * @param current   当前页
     * @param size      每页大小
     * @param studentId 学生 ID（管理员查询时使用）
     * @param status    审核状态（0-待审核 1-导师审核通过 2-院系审核通过 3-审核不通过）
     * @param keyword   关键词（学生姓名或学号，仅导师/管理员使用）
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询论文", description = "支持按学生 ID、审核状态、关键词筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<ResearchPaperVO>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "审核状态", example = "1") @RequestParam(required = false) Integer status,
            @Parameter(description = "关键词（学生姓名或学号）", example = "张三") @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {
        IPage<ResearchPaperVO> page = researchPaperService.pagePapersWithUser(current, size, studentId, status, keyword, loginUser);
        return Result.success(page);
    }

    /**
     * 获取论文详情
     *
     * @param id 论文 ID
     * @return 论文详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取论文详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "论文不存在")
    })
    public Result<ResearchPaper> getById(@PathVariable Long id) {
        ResearchPaper paper = researchPaperService.getById(id);
        if (paper == null) {
            return Result.error("论文不存在");
        }
        return Result.success(paper);
    }

    /**
     * 学生提交论文
     *
     * @param paper 论文信息
     * @return 是否成功
     */
    @PostMapping("/submit")
    @Operation(summary = "提交论文", description = "研究生提交论文成果，需填写论文标题、作者、期刊等信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "提交成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public Result<Void> submit(
            @Valid @RequestBody ResearchPaper paper,
            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchPaperService.submitPaper(paper, loginUser.getUserId());
        return success ? Result.success("提交成功") : Result.error("提交失败");
    }

    /**
     * 导师审核论文
     *
     * @param id           论文 ID
     * @param status       审核状态
     * @param reviewComment 审核意见
     * @return 是否成功
     */
    @PutMapping("/review/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TUTOR', 'ROLE_ADMIN')")
    @Operation(summary = "审核论文", description = "导师或管理员对论文进行审核")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "审核成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "论文不存在")
    })
    public Result<Void> review(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "审核参数")
            @RequestBody ReviewPaperRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {
        Long reviewerId = loginUser.getUserId();
        boolean success = researchPaperService.reviewPaper(id, request.status(), request.reviewComment(), reviewerId);
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }

    /**
     * 审核论文请求体
     */
    public record ReviewPaperRequest(
        Integer status,
        String reviewComment
    ) {}

    /**
     * 删除论文（仅草稿状态可删除）
     *
     * @param id 论文 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "删除论文", description = "仅草稿状态（status=0）的论文可以删除")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "400", description = "已提交的论文不能删除"),
        @ApiResponse(responseCode = "404", description = "论文不存在"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser loginUser) {
        boolean isAdmin = loginUser.getUserType() == 3;
        boolean success = researchPaperService.deleteWithAuth(id, loginUser.getUserId(), isAdmin);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
