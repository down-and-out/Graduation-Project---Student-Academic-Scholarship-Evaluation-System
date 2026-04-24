package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.entity.StudentInfo;
import com.scholarship.exception.BusinessException;
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
        applyDataScope(wrapper, studentId, loginUser);
        wrapper.eq(auditStatus != null, ResearchPatent::getAuditStatus, auditStatus)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(ResearchPatent::getPatentName, keyword)
                        .or().like(ResearchPatent::getPatentNo, keyword)
                        .or().like(ResearchPatent::getInventors, keyword)
                        .or().like(ResearchPatent::getApplicant, keyword))
                .orderByDesc(ResearchPatent::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public ResearchPatent getPatentById(Long id, LoginUser loginUser) {
        ResearchPatent patent = getById(id);
        if (patent == null) {
            return null;
        }
        ensureReadable(patent.getStudentId(), loginUser);
        return patent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePatent(ResearchPatent patent, LoginUser loginUser) {
        if (loginUser == null) {
            throw new BusinessException("当前登录用户不存在");
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = requireStudentByUserId(loginUser.getUserId());
            patent.setStudentId(currentStudent.getId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            requireStudentById(patent.getStudentId());
        } else {
            throw new BusinessException("无权新增专利成果");
        }

        if (patent.getAuditStatus() == null) {
            patent.setAuditStatus(0);
        }
        if (patent.getScore() == null) {
            patent.setScore(BigDecimal.ZERO);
        }
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
            StudentInfo currentStudent = requireStudentByUserId(loginUser.getUserId());
            if (!existing.getStudentId().equals(currentStudent.getId())) {
                log.warn("用户 {} 尝试更新不属于自己的专利 {}", loginUser.getUserId(), patent.getId());
                throw new BusinessException("无权更新该专利成果");
            }
            patent.setStudentId(existing.getStudentId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            if (patent.getStudentId() == null) {
                patent.setStudentId(existing.getStudentId());
            } else {
                requireStudentById(patent.getStudentId());
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
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long id, Integer auditStatus, String auditComment) {
        log.info("审核专利，id={}, auditStatus={}", id, auditStatus);

        ResearchPatent patent = getById(id);
        if (patent == null) {
            throw new BusinessException("专利不存在");
        }

        patent.setAuditStatus(auditStatus);
        patent.setAuditComment(auditComment);
        patent.setAuditTime(LocalDateTime.now());
        return updateById(patent);
    }

    private void applyDataScope(LambdaQueryWrapper<ResearchPatent> wrapper, Long requestedStudentId, LoginUser loginUser) {
        if (loginUser == null || UserTypeEnum.isAdmin(loginUser.getUserType())) {
            wrapper.eq(requestedStudentId != null, ResearchPatent::getStudentId, requestedStudentId);
            return;
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = findStudentByUserId(loginUser.getUserId());
            wrapper.eq(ResearchPatent::getStudentId, currentStudent == null ? -1L : currentStudent.getId());
            return;
        }

        if (UserTypeEnum.isTutor(loginUser.getUserType())) {
            List<Long> studentIds = listStudentIdsByTutorId(loginUser.getUserId());
            if (studentIds.isEmpty()) {
                wrapper.eq(ResearchPatent::getStudentId, -1L);
            } else {
                wrapper.in(ResearchPatent::getStudentId, studentIds);
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
                throw new BusinessException("无权查看该专利成果");
            }
            return;
        }
        if (UserTypeEnum.isTutor(loginUser.getUserType()) && listStudentIdsByTutorId(loginUser.getUserId()).contains(ownerStudentId)) {
            return;
        }
        throw new BusinessException("无权查看该专利成果");
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
