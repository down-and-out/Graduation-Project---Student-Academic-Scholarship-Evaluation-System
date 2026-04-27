package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.util.DataScopeHelper;
import com.scholarship.entity.CompetitionAward;
import com.scholarship.entity.StudentInfo;
import com.scholarship.common.exception.BusinessException;
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
import java.time.LocalDateTime;
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
        DataScopeHelper.applyDataScope(wrapper, studentId, loginUser, CompetitionAward::getStudentId, studentInfoMapper);
        if (StringUtils.hasText(keyword)) {
            List<Long> keywordStudentIds = DataScopeHelper.listStudentsByKeyword(keyword, studentInfoMapper).stream()
                    .map(StudentInfo::getId)
                    .toList();
            wrapper.and(w -> w
                    .like(CompetitionAward::getCompetitionName, keyword)
                    .or().like(CompetitionAward::getInstructor, keyword)
                    .or().like(CompetitionAward::getIssuingUnit, keyword)
                    .or().like(CompetitionAward::getOrganizer, keyword)
                    .or(!keywordStudentIds.isEmpty()).in(CompetitionAward::getStudentId, keywordStudentIds));
        }
        wrapper.eq(auditStatus != null, CompetitionAward::getAuditStatus, auditStatus)
                .orderByDesc(CompetitionAward::getCreateTime);
        IPage<CompetitionAward> result = page(page, wrapper);
        DataScopeHelper.attachStudentInfo(result.getRecords(),
                CompetitionAward::getStudentId, CompetitionAward::setStudentNo, CompetitionAward::setStudentName, studentInfoMapper);
        return result;
    }

    @Override
    public CompetitionAward getAwardById(Long id, LoginUser loginUser) {
        CompetitionAward award = getById(id);
        if (award == null) {
            return null;
        }
        DataScopeHelper.ensureReadable(award.getStudentId(), loginUser, "竞赛成果", studentInfoMapper);
        return award;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long id, Integer auditStatus, String auditComment, Long auditorId, boolean isAdmin) {
        CompetitionAward award = getById(id);
        if (award == null) {
            throw new BusinessException("竞赛成果不存在");
        }

        StudentInfo student = DataScopeHelper.requireStudentById(award.getStudentId(), studentInfoMapper);
        if (!isAdmin && student.getTutorId() != null && !student.getTutorId().equals(auditorId)) {
            log.warn("用户 {} 尝试审核不属于自己的竞赛成果 {}", auditorId, id);
            throw new BusinessException("无权审核该竞赛成果");
        }

        award.setAuditStatus(auditStatus);
        award.setAuditComment(auditComment);
        award.setAuditorId(auditorId);
        award.setAuditTime(LocalDateTime.now());
        return updateById(award);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAward(CompetitionAward award, LoginUser loginUser) {
        if (loginUser == null) {
            throw new BusinessException("当前登录用户不存在");
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = DataScopeHelper.requireStudentByUserId(loginUser.getUserId(), studentInfoMapper);
            award.setStudentId(currentStudent.getId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            DataScopeHelper.requireStudentById(award.getStudentId(), studentInfoMapper);
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
            StudentInfo currentStudent = DataScopeHelper.requireStudentByUserId(loginUser.getUserId(), studentInfoMapper);
            if (!existing.getStudentId().equals(currentStudent.getId())) {
                log.warn("用户 {} 尝试更新不属于自己的竞赛成果 {}", loginUser.getUserId(), award.getId());
                throw new BusinessException("无权更新该竞赛成果");
            }
            award.setStudentId(existing.getStudentId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            if (award.getStudentId() == null) {
                award.setStudentId(existing.getStudentId());
            } else {
                DataScopeHelper.requireStudentById(award.getStudentId(), studentInfoMapper);
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

}
