package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.security.LoginUser;
import com.scholarship.vo.ResearchPaperVO;

import java.util.List;
import java.util.Map;

/**
 * 科研论文服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface ResearchPaperService extends IService<ResearchPaper> {

    /**
     * 分页查询论文
     *
     * @param current   当前页
     * @param size      每页大小
     * @param studentId 学生ID（可选）
     * @param status    审核状态（可选）
     * @return 分页结果
     */
    IPage<ResearchPaper> pagePapers(Long current, Long size, Long studentId, Integer status);

    /**
     * 分页查询论文（带用户信息和关键词搜索）
     *
     * @param current   当前页
     * @param size      每页大小
     * @param studentId 学生 ID（可选）
     * @param status    审核状态（可选）
     * @param keyword   关键词（学生姓名或学号，可选）
     * @param loginUser 当前登录用户
     * @return 分页结果（VO 对象）
     */
    IPage<ResearchPaperVO> pagePapersWithUser(Long current, Long size, Long studentId, Integer status, String keyword, LoginUser loginUser);

    /**
     * 提交论文
     *
     * @param paper 论文信息
     * @param studentId 学生 ID
     * @return 是否成功
     */
    boolean submitPaper(ResearchPaper paper, Long studentId);

    /**
     * 学生更新论文
     *
     * @param paperId 论文 ID
     * @param paper 更新后的论文信息
     * @param userId 当前登录用户 ID
     * @return 是否成功
     */
    boolean updatePaper(Long paperId, ResearchPaper paper, Long userId);

    /**
     * 审核论文
     *
     * @param paperId       论文ID
     * @param status        审核状态
     * @param reviewComment 审核意见
     * @param reviewerId    审核人ID
     * @return 是否成功
     */
    boolean reviewPaper(Long paperId, Integer status, String reviewComment, Long reviewerId, boolean isAdmin);

    /**
     * 批量查询学生的审核通过论文
     *
     * @param studentIds 学生 ID 列表
     * @return 按学生 ID 分组的论文列表
     */
    Map<Long, List<ResearchPaper>> mapByStudentIds(List<Long> studentIds);

    /**
     * 删除论文（带权限校验）
     *
     * @param id 论文 ID
     * @param currentUserId 当前用户 ID
     * @param isAdmin 是否为管理员
     * @return 是否成功
     */
    boolean deleteWithAuth(Long id, Long currentUserId, boolean isAdmin);
}
