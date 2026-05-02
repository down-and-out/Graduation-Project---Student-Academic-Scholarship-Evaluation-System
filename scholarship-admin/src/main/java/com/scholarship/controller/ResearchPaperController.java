package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.security.LoginUser;
import com.scholarship.entity.StudentInfo;
import com.scholarship.service.ResearchPaperService;
import com.scholarship.service.StudentInfoService;
import com.scholarship.vo.ResearchPaperVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
 * 功能：论文提交、查询、审核、删除
 *
 * 业务流程：
 * 1. 学生提交论文 -> 状态变为待审核
 * 2. 导师/管理员审核论文 -> 通过/驳回
 * 3. 审核通过的论文可关联到奖学金申请
 *
 * 权限说明：
 * - 提交：仅学生
 * - 分页查询/详情：导师和管理员可按学生ID筛选，学生只能看自己的
 * - 更新：仅学生且仅能更新自己的待审核论文
 * - 审核：仅导师和管理员
 * - 删除：学生仅能删除自己的待审核论文，管理员可删除任意论文
 */
@Slf4j
@RestController
@RequestMapping("/paper")
@RequiredArgsConstructor
@Tag(name = "03-科研论文管理", description = "科研论文的提交、查询、审核接口")
public class ResearchPaperController {

    private final ResearchPaperService researchPaperService;
    private final StudentInfoService studentInfoService;

    /**
     * 分页查询论文
     * 根据登录用户角色自动过滤数据范围：
     * - 管理员/导师：可查看所有论文（按关键词/审核状态筛选）
     * - 学生：仅能查看自己的论文
     *
     * @param current    当前页码
     * @param size       每页条数
     * @param studentId  筛选指定学生的论文（管理员/导师用）
     * @param status     审核状态筛选
     * @param keyword    关键词（匹配学生姓名或学号）
     * @param loginUser  当前登录用户
     * @return 分页后的论文列表（含学生姓名快照）
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询论文", description = "支持按学生、审核状态和关键词筛选论文")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<ResearchPaperVO>> page(
            @Parameter(description = "当前页", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) Long current,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Max(100) Long size,
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
     *
     * @param id        论文ID
     * @param loginUser 当前登录用户
     * @return 论文详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取论文详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "论文不存在")
    })
    public Result<ResearchPaper> getById(@PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser) {
        ResearchPaper paper = researchPaperService.getPaperById(id, loginUser);
        if (paper == null) {
            return Result.error("论文不存在");
        }
        return Result.success(paper);
    }

    /**
     * 学生提交论文
     * 自动将当前用户绑定为论文所属学生，并设置初始状态为待审核
     *
     * @param paper     论文信息（不含studentId，由系统自动填充）
     * @param loginUser 当前登录学生
     * @return 操作结果
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
     * 仅允许更新自己的待审核论文，更新后状态保持不变
     *
     * @param id        论文ID
     * @param paper     更新后的论文信息
     * @param loginUser 当前登录学生
     * @return 操作结果
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
     * 审核通过后论文可用于奖学金申请关联
     *
     * @param id        论文ID
     * @param request   审核参数（状态、审核意见）
     * @param loginUser 当前登录用户
     * @return 操作结果
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
                UserTypeEnum.isAdmin(loginUser.getUserType())
        );
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }

    /**
     * 审核请求体
     * @param status        审核状态（1=导师通过，3=不通过）
     * @param reviewComment 审核意见
     */
    public record ReviewPaperRequest(
            Integer status,
            String reviewComment
    ) {
    }

    /**
     * 删除论文
     * 仅待审核状态的论文允许删除
     *
     * @param id        论文ID
     * @param loginUser 当前登录用户
     * @return 操作结果
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
        boolean isAdmin = UserTypeEnum.isAdmin(loginUser.getUserType());
        boolean success = researchPaperService.deleteWithAuth(id, loginUser.getUserId(), isAdmin);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 统计当前学生审核通过的论文数量
     * 用于前端显示成果统计
     *
     * @param loginUser 当前登录学生
     * @return 通过审核的论文数量
     */
    @GetMapping("/count")
    @Operation(summary = "统计当前学生审核通过的论文数量")
    public Result<Long> count(@AuthenticationPrincipal LoginUser loginUser) {
        StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
        if (studentInfo == null) {
            return Result.success(0L);
        }
        return Result.success(researchPaperService.countOwnedByStudentId(studentInfo.getId()));
    }
}
