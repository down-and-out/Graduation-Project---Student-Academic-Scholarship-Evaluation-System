package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.util.DataScopeHelper;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.entity.StudentInfo;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.mapper.ResearchPatentMapper;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchPatentService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchPatentServiceImpl extends ServiceImpl<ResearchPatentMapper, ResearchPatent> implements ResearchPatentService {

    private final StudentInfoMapper studentInfoMapper;

    @Override
    public IPage<ResearchPatent> pagePatents(Page<ResearchPatent> page, Long studentId, Integer auditStatus,
                                             String keyword, LoginUser loginUser) {
        LambdaQueryWrapper<ResearchPatent> wrapper = new LambdaQueryWrapper<>();
        DataScopeHelper.applyDataScope(wrapper, studentId, loginUser, ResearchPatent::getStudentId, studentInfoMapper);
        if (StringUtils.hasText(keyword)) {
            List<Long> keywordStudentIds = DataScopeHelper.listStudentsByKeyword(keyword, studentInfoMapper).stream()
                    .map(StudentInfo::getId)
                    .toList();
            wrapper.and(w -> w
                    .like(ResearchPatent::getPatentName, keyword)
                    .or().like(ResearchPatent::getPatentNo, keyword)
                    .or().like(ResearchPatent::getInventors, keyword)
                    .or().like(ResearchPatent::getApplicant, keyword)
                    .or(!keywordStudentIds.isEmpty()).in(ResearchPatent::getStudentId, keywordStudentIds));
        }
        wrapper.eq(auditStatus != null, ResearchPatent::getAuditStatus, auditStatus)
                .orderByDesc(ResearchPatent::getCreateTime);
        IPage<ResearchPatent> result = page(page, wrapper);
        DataScopeHelper.attachStudentInfo(result.getRecords(),
                ResearchPatent::getStudentId, ResearchPatent::setStudentNo, ResearchPatent::setStudentName, studentInfoMapper);
        return result;
    }

    @Override
    public ResearchPatent getPatentById(Long id, LoginUser loginUser) {
        ResearchPatent patent = getById(id);
        if (patent == null) {
            return null;
        }
        DataScopeHelper.ensureReadable(patent.getStudentId(), loginUser, "专利成果", studentInfoMapper);
        return patent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePatent(ResearchPatent patent, LoginUser loginUser) {
        if (loginUser == null) {
            throw new BusinessException("当前登录用户不存在");
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = DataScopeHelper.requireStudentByUserId(loginUser.getUserId(), studentInfoMapper);
            patent.setStudentId(currentStudent.getId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            DataScopeHelper.requireStudentById(patent.getStudentId(), studentInfoMapper);
        } else {
            throw new BusinessException("无权新增专利成果");
        }

        if (patent.getAuditStatus() == null) {
            patent.setAuditStatus(0);
        }
        if (patent.getScore() == null) {
            patent.setScore(BigDecimal.ZERO);
        }
        patent.setDeleted(0);
        patent.setCreateTime(LocalDateTime.now());
        return save(patent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePatent(ResearchPatent patent, LoginUser loginUser) {
        if (loginUser == null || patent.getId() == null) {
            throw new BusinessException("缺少专利或登录用户信息");
        }

        ResearchPatent existing = getById(patent.getId());
        if (existing == null) {
            throw new BusinessException("专利不存在");
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = DataScopeHelper.requireStudentByUserId(loginUser.getUserId(), studentInfoMapper);
            if (!existing.getStudentId().equals(currentStudent.getId())) {
                log.warn("用户 {} 尝试更新不属于自己的专利 {}", loginUser.getUserId(), patent.getId());
                throw new BusinessException("无权更新该专利成果");
            }
            patent.setStudentId(existing.getStudentId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            if (patent.getStudentId() == null) {
                patent.setStudentId(existing.getStudentId());
            } else {
                DataScopeHelper.requireStudentById(patent.getStudentId(), studentInfoMapper);
            }
        } else {
            throw new BusinessException("无权更新专利成果");
        }

        patent.setAuditStatus(existing.getAuditStatus());
        patent.setAuditorId(existing.getAuditorId());
        patent.setAuditTime(existing.getAuditTime());
        patent.setAuditComment(existing.getAuditComment());
        patent.setScore(existing.getScore());
        return updateById(patent);
    }

    @Override
    public Map<Long, List<ResearchPatent>> mapByStudentIds(List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        List<ResearchPatent> patents = list(new LambdaQueryWrapper<ResearchPatent>()
                .in(ResearchPatent::getStudentId, studentIds)
                .eq(ResearchPatent::getAuditStatus, 1));

        return patents.stream().collect(Collectors.groupingBy(ResearchPatent::getStudentId));
    }

    @Override
    public long countByStudentId(Long studentId) {
        return count(new LambdaQueryWrapper<ResearchPatent>()
                .eq(ResearchPatent::getStudentId, studentId)
                .eq(ResearchPatent::getAuditStatus, 1));
    }

    @Override
    public long countOwnedByStudentId(Long studentId) {
        return count(new LambdaQueryWrapper<ResearchPatent>()
                .eq(ResearchPatent::getStudentId, studentId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long id, Integer auditStatus, String auditComment, Long auditorId, boolean isAdmin) {
        log.info("审核专利，id={}, auditStatus={}", id, auditStatus);

        ResearchPatent patent = getById(id);
        if (patent == null) {
            throw new BusinessException("专利不存在");
        }

        StudentInfo student = DataScopeHelper.requireStudentById(patent.getStudentId(), studentInfoMapper);
        if (!isAdmin && student.getTutorId() != null && !student.getTutorId().equals(auditorId)) {
            log.warn("用户 {} 尝试审核不属于自己的专利 {}", auditorId, id);
            throw new BusinessException("无权审核该专利成果");
        }

        patent.setAuditStatus(auditStatus);
        patent.setAuditComment(auditComment);
        patent.setAuditTime(LocalDateTime.now());
        patent.setAuditorId(auditorId);
        return updateById(patent);
    }

}
