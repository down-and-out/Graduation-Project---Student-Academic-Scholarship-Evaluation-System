package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.ResearchPaperMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchPaperService;
import com.scholarship.service.StudentInfoService;
import com.scholarship.vo.ResearchPaperVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 科研论文服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchPaperServiceImpl extends ServiceImpl<ResearchPaperMapper, ResearchPaper> implements ResearchPaperService {

    private final ResearchPaperMapper researchPaperMapper;
    private final StudentInfoService studentInfoService;

    @Override
    public IPage<ResearchPaper> pagePapers(Long current, Long size, Long studentId, Integer status) {
        Page<ResearchPaper> page = new Page<>(current, size);

        LambdaQueryWrapper<ResearchPaper> wrapper = new LambdaQueryWrapper<>();

        if (studentId != null) {
            wrapper.eq(ResearchPaper::getStudentId, studentId);
        }

        if (status != null) {
            wrapper.eq(ResearchPaper::getStatus, status);
        }

        wrapper.orderByDesc(ResearchPaper::getPublicationDate);

        return researchPaperMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<ResearchPaperVO> pagePapersWithUser(Long current, Long size, Long studentId, Integer status, String keyword, LoginUser loginUser) {
        Page<ResearchPaper> page = new Page<>(current, size);
        LambdaQueryWrapper<ResearchPaper> wrapper = new LambdaQueryWrapper<>();

        // 根据用户类型过滤数据
        if (loginUser != null) {
            if (loginUser.getUserType() == 1) {
                // 学生用户只能看到自己的论文
                StudentInfo currentStudent = studentInfoService.getByUserId(loginUser.getUserId());
                if (currentStudent == null) {
                    Page<ResearchPaper> emptyPage = new Page<>(current, size, 0);
                    return convertToVOPage(emptyPage, new ArrayList<>());
                }
                wrapper.eq(ResearchPaper::getStudentId, currentStudent.getId());
            } else if (loginUser.getUserType() == 2) {
                // 导师用户可以看到所指导学生的论文
                List<StudentInfo> students = studentInfoService.list(new LambdaQueryWrapper<StudentInfo>()
                    .eq(StudentInfo::getTutorId, loginUser.getUserId()));
                List<Long> studentIds = students.stream()
                    .map(StudentInfo::getId)
                    .collect(Collectors.toList());
                if (studentIds.isEmpty()) {
                    // 如果没有指导学生，返回空结果
                    Page<ResearchPaper> emptyPage = new Page<>(current, size, 0);
                    return convertToVOPage(emptyPage, new ArrayList<>());
                }
                wrapper.in(ResearchPaper::getStudentId, studentIds);
            }
            // 管理员可以看到所有论文
        }

        // 关键词搜索（学生姓名或学号）
        if (StringUtils.hasText(keyword)) {
            List<StudentInfo> students = studentInfoService.list(new LambdaQueryWrapper<StudentInfo>()
                .and(w -> w.like(StringUtils.hasText(keyword), StudentInfo::getStudentNo, keyword)
                    .or()
                    .like(StringUtils.hasText(keyword), StudentInfo::getName, keyword)));
            List<Long> studentIds = students.stream()
                .map(StudentInfo::getId)
                .collect(Collectors.toList());
            if (studentIds.isEmpty()) {
                Page<ResearchPaper> emptyPage = new Page<>(current, size, 0);
                return convertToVOPage(emptyPage, new ArrayList<>());
            }
            wrapper.in(ResearchPaper::getStudentId, studentIds);
        }

        if (studentId != null) {
            wrapper.eq(ResearchPaper::getStudentId, studentId);
        }

        if (status != null) {
            wrapper.eq(ResearchPaper::getStatus, status);
        }

        wrapper.orderByDesc(ResearchPaper::getCreateTime);

        Page<ResearchPaper> paperPage = researchPaperMapper.selectPage(page, wrapper);

        // 获取学生信息并转换为 VO
        List<ResearchPaper> papers = paperPage.getRecords();
        List<Long> studentIds = papers.stream()
            .map(ResearchPaper::getStudentId)
            .distinct()
            .collect(Collectors.toList());

        List<StudentInfo> students = new ArrayList<>();
        if (!studentIds.isEmpty()) {
            students = studentInfoService.list(new LambdaQueryWrapper<StudentInfo>()
                .in(StudentInfo::getId, studentIds));
        }

        return convertToVOPage(paperPage, students);
    }

    /**
     * 将 ResearchPaper 分页结果转换为 ResearchPaperVO 分页结果
     */
    private Page<ResearchPaperVO> convertToVOPage(Page<ResearchPaper> paperPage, List<StudentInfo> students) {
        Page<ResearchPaperVO> voPage = new Page<>(paperPage.getCurrent(), paperPage.getSize(), paperPage.getTotal());

        Map<Long, StudentInfo> studentMap = students.stream()
            .collect(Collectors.toMap(StudentInfo::getId, s -> s));

        List<ResearchPaperVO> voList = new ArrayList<>();
        for (ResearchPaper paper : paperPage.getRecords()) {
            ResearchPaperVO vo = new ResearchPaperVO();
            vo.setId(paper.getId());
            vo.setStudentId(paper.getStudentId());

            // 设置学生信息
            StudentInfo student = studentMap.get(paper.getStudentId());
            if (student != null) {
                vo.setStudentNo(student.getStudentNo());
                vo.setStudentName(student.getName());
            }

            // 固定类型为 paper
            vo.setType("paper");

            // 论文标题映射为成果名称
            vo.setTitle(paper.getPaperTitle());
            vo.setPaperTitle(paper.getPaperTitle());
            vo.setJournalName(paper.getJournalName());
            vo.setJournalLevel(paper.getJournalLevel());
            vo.setImpactFactor(paper.getImpactFactor());

            // 期刊级别映射为级别文本
            if (paper.getJournalLevel() != null) {
                String[] levels = {"", "SCI 一区", "SCI 二区", "SCI 三区", "SCI 四区", "EI", "核心期刊", "普通期刊"};
                vo.setLevel(paper.getJournalLevel() >= 1 && paper.getJournalLevel() <= 7 ? levels[paper.getJournalLevel()] : "");
            }

            // 影响因子映射为分值
            vo.setScore(paper.getImpactFactor() != null ? paper.getImpactFactor() : java.math.BigDecimal.ZERO);
            vo.setAuthors(paper.getAuthors());
            vo.setAuthorRank(paper.getAuthorRank());
            vo.setDate(paper.getPublicationDate());
            vo.setPublicationDate(paper.getPublicationDate());
            vo.setSubmitTime(paper.getCreateTime());
            vo.setStatus(paper.getStatus());
            vo.setReviewComment(paper.getReviewComment());
            vo.setReviewerId(paper.getReviewerId());
            vo.setReviewTime(paper.getReviewTime());
            vo.setCreateTime(paper.getCreateTime());

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitPaper(ResearchPaper paper, Long studentId) {
        // 设置学生 ID
        StudentInfo studentInfo = studentInfoService.getByUserId(studentId);
        if (studentInfo == null) {
            throw new BusinessException("瀛︾敓淇℃伅涓嶅瓨鍦?");
        }

        paper.setStudentId(studentInfo.getId());
        paper.setStatus(0); // 待审核
        paper.setCreateTime(LocalDateTime.now());
        return researchPaperMapper.insert(paper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewPaper(Long paperId, Integer status, String reviewComment, Long reviewerId, boolean isAdmin) {
        ResearchPaper paper = researchPaperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("论文不存在");
        }

        // 获取论文作者（学生）的导师 ID
        StudentInfo student = studentInfoService.getById(paper.getStudentId());
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 如果学生有导师，验证当前审核人是否为该导师
        if (!isAdmin && student.getTutorId() != null) {
            if (!student.getTutorId().equals(reviewerId)) {
                log.warn("审核人 {} 尝试审核不属于她指导的学生的论文 {}", reviewerId, paperId);
                throw new BusinessException("无权审核该论文，只能审核自己指导学生的论文");
            }
        }

        paper.setStatus(status);
        paper.setReviewComment(reviewComment);
        paper.setReviewerId(reviewerId);
        paper.setReviewTime(LocalDateTime.now());

        return researchPaperMapper.updateById(paper) > 0;
    }

    @Override
    public Map<Long, List<ResearchPaper>> mapByStudentIds(List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        List<ResearchPaper> papers = list(new LambdaQueryWrapper<ResearchPaper>()
            .in(ResearchPaper::getStudentId, studentIds)
            .eq(ResearchPaper::getStatus, 2)); // 审核通过

        return papers.stream()
            .collect(Collectors.groupingBy(ResearchPaper::getStudentId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithAuth(Long id, Long currentUserId, boolean isAdmin) {
        log.info("删除论文，id={}, currentUserId={}, isAdmin={}", id, currentUserId, isAdmin);

        ResearchPaper paper = researchPaperMapper.selectById(id);
        if (paper == null) {
            throw new com.scholarship.exception.BusinessException("论文不存在");
        }

        // 权限验证：只有论文所有者或管理员可以删除
        Long currentStudentId = currentUserId;
        if (!isAdmin) {
            StudentInfo currentStudent = studentInfoService.getByUserId(currentUserId);
            if (currentStudent == null) {
                throw new com.scholarship.exception.BusinessException("学生信息不存在");
            }
            currentStudentId = currentStudent.getId();
        }

        if (!paper.getStudentId().equals(currentStudentId) && !isAdmin) {
            throw new com.scholarship.exception.BusinessException("无权限删除该论文");
        }

        // 状态检查：只有草稿状态（status=0）的论文可以删除
        if (paper.getStatus() != 0) {
            throw new com.scholarship.exception.BusinessException("已提交的论文不能删除");
        }

        return removeById(id);
    }
}
