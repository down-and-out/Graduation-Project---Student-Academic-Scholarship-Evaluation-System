package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.util.DataScopeHelper;
import com.scholarship.entity.ResearchProject;
import com.scholarship.entity.StudentInfo;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.mapper.ResearchProjectMapper;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchProjectService;
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
 * 科研项目服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchProjectServiceImpl extends ServiceImpl<ResearchProjectMapper, ResearchProject> implements ResearchProjectService {

    private final StudentInfoMapper studentInfoMapper;

    @Override
    public IPage<ResearchProject> pageProjects(Page<ResearchProject> page, Long studentId, Integer auditStatus,
                                               String keyword, LoginUser loginUser) {
        LambdaQueryWrapper<ResearchProject> wrapper = new LambdaQueryWrapper<>();
        DataScopeHelper.applyDataScope(wrapper, studentId, loginUser, ResearchProject::getStudentId, studentInfoMapper);
        if (StringUtils.hasText(keyword)) {
            List<Long> keywordStudentIds = DataScopeHelper.listStudentsByKeyword(keyword, studentInfoMapper).stream()
                    .map(StudentInfo::getId)
                    .toList();
            wrapper.and(w -> w
                    .like(ResearchProject::getProjectName, keyword)
                    .or().like(ResearchProject::getProjectNo, keyword)
                    .or().like(ResearchProject::getProjectSource, keyword)
                    .or().like(ResearchProject::getLeaderName, keyword)
                    .or(!keywordStudentIds.isEmpty()).in(ResearchProject::getStudentId, keywordStudentIds));
        }
        wrapper.eq(auditStatus != null, ResearchProject::getAuditStatus, auditStatus)
                .orderByDesc(ResearchProject::getCreateTime);
        IPage<ResearchProject> result = page(page, wrapper);
        DataScopeHelper.attachStudentInfo(result.getRecords(),
                ResearchProject::getStudentId, ResearchProject::setStudentNo, ResearchProject::setStudentName, studentInfoMapper);
        return result;
    }

    @Override
    public ResearchProject getProjectById(Long id, LoginUser loginUser) {
        ResearchProject project = getById(id);
        if (project == null) {
            return null;
        }
        DataScopeHelper.ensureReadable(project.getStudentId(), loginUser, "项目成果", studentInfoMapper);
        return project;
    }

    @Override
    public long countByStudentId(Long studentId) {
        return count(new LambdaQueryWrapper<ResearchProject>()
                .eq(ResearchProject::getStudentId, studentId)
                .eq(ResearchProject::getAuditStatus, 1));
    }

    @Override
    public long countOwnedByStudentId(Long studentId) {
        return count(new LambdaQueryWrapper<ResearchProject>()
                .eq(ResearchProject::getStudentId, studentId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long id, Integer auditStatus, String auditComment, Long auditorId, boolean isAdmin) {
        ResearchProject project = getById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        log.info("审核项目，id={}, auditStatus={}", id, auditStatus);

        StudentInfo student = DataScopeHelper.requireStudentById(project.getStudentId(), studentInfoMapper);
        if (!isAdmin && student.getTutorId() != null && !student.getTutorId().equals(auditorId)) {
            log.warn("用户 {} 尝试审核不属于自己的项目 {}", auditorId, id);
            throw new BusinessException("无权审核该项目成果");
        }

        // 状态机校验：仅待审核状态(auditStatus=0)允许审核
        if (project.getAuditStatus() != 0) {
            throw new BusinessException("仅待审核的项目允许审核");
        }
        // 状态机校验：审核状态只能为1(通过)或2(驳回)
        if (auditStatus != 1 && auditStatus != 2) {
            throw new BusinessException("无效的审核状态，允许值：1(通过)、2(驳回)");
        }

        // 条件更新 + 并发保护：仅当状态仍为0时才执行更新
        LambdaUpdateWrapper<ResearchProject> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ResearchProject::getId, id)
                .eq(ResearchProject::getAuditStatus, 0)
                .set(ResearchProject::getAuditStatus, auditStatus)
                .set(ResearchProject::getAuditComment, auditComment)
                .set(ResearchProject::getAuditorId, auditorId)
                .set(ResearchProject::getAuditTime, LocalDateTime.now());
        int updated = baseMapper.update(null, updateWrapper);
        if (updated == 0) {
            throw new BusinessException("状态已变化，请刷新后重试");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveProject(ResearchProject project, LoginUser loginUser) {
        if (loginUser == null) {
            throw new BusinessException("当前登录用户不存在");
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = DataScopeHelper.requireStudentByUserId(loginUser.getUserId(), studentInfoMapper);
            project.setStudentId(currentStudent.getId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            DataScopeHelper.requireStudentById(project.getStudentId(), studentInfoMapper);
        } else {
            throw new BusinessException("无权新增项目成果");
        }

        if (project.getAuditStatus() == null) {
            project.setAuditStatus(0);
        }
        if (project.getScore() == null) {
            project.setScore(BigDecimal.ZERO);
        }
        project.setDeleted(0);
        project.setCreateTime(LocalDateTime.now());
        return save(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProject(ResearchProject project, LoginUser loginUser) {
        if (loginUser == null || project.getId() == null) {
            throw new BusinessException("缺少项目或登录用户信息");
        }

        ResearchProject existing = getById(project.getId());
        if (existing == null) {
            throw new BusinessException("项目不存在");
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = DataScopeHelper.requireStudentByUserId(loginUser.getUserId(), studentInfoMapper);
            if (!existing.getStudentId().equals(currentStudent.getId())) {
                log.warn("用户 {} 尝试更新不属于自己的项目 {}", loginUser.getUserId(), project.getId());
                throw new BusinessException("无权更新该项目成果");
            }
            project.setStudentId(existing.getStudentId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            if (project.getStudentId() == null) {
                project.setStudentId(existing.getStudentId());
            } else {
                DataScopeHelper.requireStudentById(project.getStudentId(), studentInfoMapper);
            }
        } else {
            throw new BusinessException("无权更新项目成果");
        }

        project.setAuditStatus(existing.getAuditStatus());
        project.setAuditorId(existing.getAuditorId());
        project.setAuditTime(existing.getAuditTime());
        project.setAuditComment(existing.getAuditComment());
        project.setScore(existing.getScore());
        return updateById(project);
    }

    @Override
    public Map<Long, List<ResearchProject>> mapByStudentIds(List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        List<ResearchProject> projects = list(new LambdaQueryWrapper<ResearchProject>()
                .in(ResearchProject::getStudentId, studentIds)
                .eq(ResearchProject::getAuditStatus, 1));

        return projects.stream().collect(Collectors.groupingBy(ResearchProject::getStudentId));
    }

}
