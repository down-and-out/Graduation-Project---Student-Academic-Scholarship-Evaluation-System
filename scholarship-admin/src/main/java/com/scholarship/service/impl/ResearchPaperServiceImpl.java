package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.PaperLevelEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.util.DataScopeHelper;
import com.scholarship.entity.ResearchPaper;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.ResearchPaperMapper;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchPaperService;
import com.scholarship.vo.ResearchPaperVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchPaperServiceImpl extends ServiceImpl<ResearchPaperMapper, ResearchPaper> implements ResearchPaperService {

    private final ResearchPaperMapper researchPaperMapper;
    private final StudentInfoMapper studentInfoMapper;

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

        DataScopeHelper.applyDataScope(wrapper, studentId, loginUser, ResearchPaper::getStudentId, studentInfoMapper);

        if (StringUtils.hasText(keyword)) {
            List<StudentInfo> students = DataScopeHelper.listStudentsByKeyword(keyword, studentInfoMapper);
            List<Long> keywordStudentIds = students.stream().map(StudentInfo::getId).toList();
            if (keywordStudentIds.isEmpty()) {
                return convertToVOPage(new Page<>(current, size, 0), List.of());
            }
            wrapper.in(ResearchPaper::getStudentId, keywordStudentIds);
        }

        if (studentId != null) {
            wrapper.eq(ResearchPaper::getStudentId, studentId);
        }
        if (status != null) {
            wrapper.eq(ResearchPaper::getStatus, status);
        }

        wrapper.orderByDesc(ResearchPaper::getCreateTime);

        Page<ResearchPaper> paperPage = researchPaperMapper.selectPage(page, wrapper);
        List<Long> studentIds = paperPage.getRecords().stream()
                .map(ResearchPaper::getStudentId)
                .distinct()
                .toList();

        List<StudentInfo> students = studentIds.isEmpty()
                ? List.of()
                : new ArrayList<>(mapStudentsByIds(studentIds).values());

        return convertToVOPage(paperPage, students);
    }

    private Page<ResearchPaperVO> convertToVOPage(Page<ResearchPaper> paperPage, List<StudentInfo> students) {
        Page<ResearchPaperVO> voPage = new Page<>(paperPage.getCurrent(), paperPage.getSize(), paperPage.getTotal());
        Map<Long, StudentInfo> studentMap = students.stream()
                .collect(Collectors.toMap(StudentInfo::getId, Function.identity(), (a, b) -> a));

        List<ResearchPaperVO> voList = new ArrayList<>();
        for (ResearchPaper paper : paperPage.getRecords()) {
            ResearchPaperVO vo = new ResearchPaperVO();
            vo.setId(paper.getId());
            vo.setStudentId(paper.getStudentId());

            StudentInfo student = studentMap.get(paper.getStudentId());
            if (student != null) {
                vo.setStudentNo(student.getStudentNo());
                vo.setStudentName(student.getName());
            }

            vo.setType("paper");
            vo.setTitle(paper.getPaperTitle());
            vo.setPaperTitle(paper.getPaperTitle());
            vo.setJournalName(paper.getJournalName());
            vo.setJournalLevel(paper.getJournalLevel());
            vo.setImpactFactor(paper.getImpactFactor());

            if (paper.getJournalLevel() != null) {
                PaperLevelEnum level = PaperLevelEnum.valueOfCode(paper.getJournalLevel());
                vo.setLevel(level != null ? level.getDescription() : "");
            }

            vo.setScore(paper.getImpactFactor() != null ? paper.getImpactFactor() : BigDecimal.ZERO);
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
        StudentInfo studentInfo = DataScopeHelper.findStudentByUserId(studentId, studentInfoMapper);
        if (studentInfo == null) {
            throw new BusinessException("学生信息不存在");
        }

        paper.setStudentId(studentInfo.getId());
        paper.setStatus(0);
        paper.setCreateTime(LocalDateTime.now());
        return researchPaperMapper.insert(paper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePaper(Long paperId, ResearchPaper paper, Long userId) {
        ResearchPaper existingPaper = researchPaperMapper.selectById(paperId);
        if (existingPaper == null) {
            throw new BusinessException("论文不存在");
        }

        StudentInfo currentStudent = DataScopeHelper.findStudentByUserId(userId, studentInfoMapper);
        if (currentStudent == null) {
            throw new BusinessException("学生信息不存在");
        }

        if (!existingPaper.getStudentId().equals(currentStudent.getId())) {
            throw new BusinessException("无权编辑该论文");
        }

        if (existingPaper.getStatus() != 0) {
            throw new BusinessException("仅待审核论文允许编辑");
        }

        existingPaper.setPaperTitle(paper.getPaperTitle());
        existingPaper.setAuthors(paper.getAuthors());
        existingPaper.setAuthorRank(paper.getAuthorRank());
        existingPaper.setJournalName(paper.getJournalName());
        existingPaper.setJournalLevel(paper.getJournalLevel());
        existingPaper.setImpactFactor(paper.getImpactFactor());
        existingPaper.setPublicationDate(paper.getPublicationDate());
        existingPaper.setVolume(paper.getVolume());
        existingPaper.setIssue(paper.getIssue());
        existingPaper.setPages(paper.getPages());
        existingPaper.setDoi(paper.getDoi());
        existingPaper.setIndexing(paper.getIndexing());
        existingPaper.setAttachmentUrl(paper.getAttachmentUrl());
        existingPaper.setRemark(paper.getRemark());

        return researchPaperMapper.updateById(existingPaper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewPaper(Long paperId, Integer status, String reviewComment, Long reviewerId, boolean isAdmin) {
        ResearchPaper paper = researchPaperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("论文不存在");
        }

        StudentInfo student = studentInfoMapper.selectById(paper.getStudentId());
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        if (!isAdmin && student.getTutorId() != null && !student.getTutorId().equals(reviewerId)) {
            log.warn("审核人 {} 尝试审核不属于自己指导学生的论文 {}", reviewerId, paperId);
            throw new BusinessException("无权审核该论文，只能审核自己指导学生的论文");
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
                .eq(ResearchPaper::getStatus, 1));

        return papers.stream().collect(Collectors.groupingBy(ResearchPaper::getStudentId));
    }

    @Override
    public long countByStudentId(Long studentId) {
        return count(new LambdaQueryWrapper<ResearchPaper>()
                .eq(ResearchPaper::getStudentId, studentId)
                .eq(ResearchPaper::getStatus, 1));
    }

    @Override
    public long countOwnedByStudentId(Long studentId) {
        return count(new LambdaQueryWrapper<ResearchPaper>()
                .eq(ResearchPaper::getStudentId, studentId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithAuth(Long id, Long currentUserId, boolean isAdmin) {
        log.info("删除论文，id={}, currentUserId={}, isAdmin={}", id, currentUserId, isAdmin);

        ResearchPaper paper = researchPaperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException("论文不存在");
        }

        Long currentStudentId = currentUserId;
        if (!isAdmin) {
            StudentInfo currentStudent = DataScopeHelper.findStudentByUserId(currentUserId, studentInfoMapper);
            if (currentStudent == null) {
                throw new BusinessException("学生信息不存在");
            }
            currentStudentId = currentStudent.getId();
        }

        if (!paper.getStudentId().equals(currentStudentId) && !isAdmin) {
            throw new BusinessException("无权限删除该论文");
        }

        if (paper.getStatus() != 0) {
            throw new BusinessException("已提交的论文不能删除");
        }

        return removeById(id);
    }

    private Map<Long, StudentInfo> mapStudentsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        return studentInfoMapper.selectList(new LambdaQueryWrapper<StudentInfo>()
                        .in(StudentInfo::getId, ids))
                .stream()
                .collect(Collectors.toMap(StudentInfo::getId, Function.identity(), (a, b) -> a));
    }
}
