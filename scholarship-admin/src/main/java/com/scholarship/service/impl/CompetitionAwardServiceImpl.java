package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.entity.CompetitionAward;
import com.scholarship.entity.StudentInfo;
import com.scholarship.exception.BusinessException;
import com.scholarship.mapper.CompetitionAwardMapper;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.CompetitionAwardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学科竞赛获奖服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionAwardServiceImpl extends ServiceImpl<CompetitionAwardMapper, CompetitionAward> implements CompetitionAwardService {

    private final StudentInfoMapper studentInfoMapper;

    @Override
    public IPage<CompetitionAward> pageAwards(Page<CompetitionAward> page, Long studentId, Integer auditStatus,
                                              String keyword, LoginUser loginUser) {
        LambdaQueryWrapper<CompetitionAward> wrapper = new LambdaQueryWrapper<>();
        applyDataScope(wrapper, studentId, loginUser);
        wrapper.eq(auditStatus != null, CompetitionAward::getAuditStatus, auditStatus)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(CompetitionAward::getCompetitionName, keyword)
                        .or().like(CompetitionAward::getInstructor, keyword)
                        .or().like(CompetitionAward::getIssuingUnit, keyword)
                        .or().like(CompetitionAward::getOrganizer, keyword))
                .orderByDesc(CompetitionAward::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public CompetitionAward getAwardById(Long id, LoginUser loginUser) {
        CompetitionAward award = getById(id);
        if (award == null) {
            return null;
        }
        ensureReadable(award.getStudentId(), loginUser);
        return award;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAward(CompetitionAward award, LoginUser loginUser) {
        if (loginUser == null) {
            throw new BusinessException("当前登录用户不存在");
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = requireStudentByUserId(loginUser.getUserId());
            award.setStudentId(currentStudent.getId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            requireStudentById(award.getStudentId());
        } else {
            throw new BusinessException("无权新增竞赛成果");
        }

        if (award.getAttachmentUrl() == null && award.getProofMaterials() != null) {
            award.setAttachmentUrl(award.getProofMaterials());
        }
        award.setProofMaterials(null);

        if (award.getAuditStatus() == null) {
            award.setAuditStatus(0);
        }
        if (award.getScore() == null) {
            award.setScore(BigDecimal.ZERO);
        }
        return save(award);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAward(CompetitionAward award, LoginUser loginUser) {
        if (loginUser == null || award.getId() == null) {
            throw new BusinessException("缺少竞赛成果或登录用户信息");
        }

        CompetitionAward existing = getById(award.getId());
        if (existing == null) {
            throw new BusinessException("竞赛成果不存在");
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = requireStudentByUserId(loginUser.getUserId());
            if (!existing.getStudentId().equals(currentStudent.getId())) {
                log.warn("用户 {} 尝试更新不属于自己的竞赛成果 {}", loginUser.getUserId(), award.getId());
                throw new BusinessException("无权更新该竞赛成果");
            }
            award.setStudentId(existing.getStudentId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            if (award.getStudentId() == null) {
                award.setStudentId(existing.getStudentId());
            } else {
                requireStudentById(award.getStudentId());
            }
        } else {
            throw new BusinessException("无权更新竞赛成果");
        }

        if (award.getAttachmentUrl() == null && award.getProofMaterials() != null) {
            award.setAttachmentUrl(award.getProofMaterials());
        }
        award.setProofMaterials(null);

        award.setAuditStatus(existing.getAuditStatus());
        award.setAuditorId(existing.getAuditorId());
        award.setAuditTime(existing.getAuditTime());
        award.setAuditComment(existing.getAuditComment());
        award.setScore(existing.getScore());
        return updateById(award);
    }

    @Override
    public Map<Long, List<CompetitionAward>> mapByStudentIds(List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        List<CompetitionAward> awards = list(new LambdaQueryWrapper<CompetitionAward>()
                .in(CompetitionAward::getStudentId, studentIds)
                .eq(CompetitionAward::getAuditStatus, 1));

        return awards.stream().collect(Collectors.groupingBy(CompetitionAward::getStudentId));
    }

    private void applyDataScope(LambdaQueryWrapper<CompetitionAward> wrapper, Long requestedStudentId, LoginUser loginUser) {
        if (loginUser == null || UserTypeEnum.isAdmin(loginUser.getUserType())) {
            wrapper.eq(requestedStudentId != null, CompetitionAward::getStudentId, requestedStudentId);
            return;
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = findStudentByUserId(loginUser.getUserId());
            wrapper.eq(CompetitionAward::getStudentId, currentStudent == null ? -1L : currentStudent.getId());
            return;
        }

        if (UserTypeEnum.isTutor(loginUser.getUserType())) {
            List<Long> studentIds = listStudentIdsByTutorId(loginUser.getUserId());
            if (studentIds.isEmpty()) {
                wrapper.eq(CompetitionAward::getStudentId, -1L);
            } else {
                wrapper.in(CompetitionAward::getStudentId, studentIds);
            }
        }
    }

    private void ensureReadable(Long ownerStudentId, LoginUser loginUser) {
        if (loginUser == null || UserTypeEnum.isAdmin(loginUser.getUserType())) {
            return;
        }
        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = requireStudentByUserId(loginUser.getUserId());
            if (!ownerStudentId.equals(currentStudent.getId())) {
                throw new BusinessException("无权查看该竞赛成果");
            }
            return;
        }
        if (UserTypeEnum.isTutor(loginUser.getUserType()) && listStudentIdsByTutorId(loginUser.getUserId()).contains(ownerStudentId)) {
            return;
        }
        throw new BusinessException("无权查看该竞赛成果");
    }

    private StudentInfo requireStudentByUserId(Long userId) {
        StudentInfo student = findStudentByUserId(userId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        return student;
    }

    private StudentInfo requireStudentById(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("学生 ID 不能为空");
        }
        StudentInfo student = studentInfoMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        return student;
    }

    private StudentInfo findStudentByUserId(Long userId) {
        return studentInfoMapper.selectOne(new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getUserId, userId)
                .last("limit 1"));
    }

    private List<Long> listStudentIdsByTutorId(Long tutorUserId) {
        return studentInfoMapper.selectList(new LambdaQueryWrapper<StudentInfo>()
                        .eq(StudentInfo::getTutorId, tutorUserId))
                .stream()
                .map(StudentInfo::getId)
                .toList();
    }
}
