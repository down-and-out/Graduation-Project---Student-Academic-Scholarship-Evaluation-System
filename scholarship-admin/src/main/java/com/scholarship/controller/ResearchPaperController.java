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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 科研论文控制器
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
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询论文", description = "支持按学生、审核状态和关键词筛选论文")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<ResearchPaperVO>> page(
            @Parameter(description = "当前页", example = "1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID", example = "1")
            @RequestParam(required = false) Long studentId,
            @Parameter(description = "审核状态", example = "1")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "关键词（学生姓名或学号）", example = "张三")
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {
        IPage<ResearchPaperVO> page = researchPaperService.pagePapersWithUser(
                current,
                size,
                studentId,
                status,
                keyword,
                loginUser
        );
        return Result.success(page);
    }

    /**
     * 获取论文详情
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
     */
    @PostMapping("/submit")
    @Operation(summary = "提交论文", description = "研究生提交论文成果")
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
     * 学生更新论文
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "更新论文", description = "仅允许学生更新自己的待审核论文")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "403", description = "无权访问"),
        @ApiResponse(responseCode = "404", description = "论文不存在")
    })
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody ResearchPaper paper,
            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchPaperService.updatePaper(id, paper, loginUser.getUserId());
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 导师或管理员审核论文
     */
    @PutMapping("/review/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TUTOR', 'ROLE_ADMIN')")
    @Operation(summary = "审核论文", description = "导师或管理员审核论文")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "审核成功"),
        @ApiResponse(responseCode = "403", description = "无权访问"),
        @ApiResponse(responseCode = "404", description = "论文不存在")
    })
    public Result<Void> review(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "审核参数")
            @RequestBody ReviewPaperRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {
        Long reviewerId = loginUser.getUserId();
        boolean success = researchPaperService.reviewPaper(
                id,
                request.status(),
                request.reviewComment(),
                reviewerId,
                loginUser.getUserType() == 3
        );
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }

    /**
     * 审核论文请求体
     */
    public record ReviewPaperRequest(
            Integer status,
            String reviewComment
    ) {
    }

    /**
     * 删除论文
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "删除论文", description = "仅待审核论文允许删除")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "400", description = "当前状态不允许删除"),
        @ApiResponse(responseCode = "404", description = "论文不存在"),
        @ApiResponse(responseCode = "403", description = "无权访问")
    })
    public Result<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser) {
        boolean isAdmin = loginUser.getUserType() == 3;
        boolean success = researchPaperService.deleteWithAuth(id, loginUser.getUserId(), isAdmin);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
