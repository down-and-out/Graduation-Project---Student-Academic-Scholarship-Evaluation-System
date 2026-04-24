package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.entity.ResearchProject;
import com.scholarship.entity.StudentInfo;
import com.scholarship.exception.BusinessException;
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
        applyDataScope(wrapper, studentId, loginUser);
        wrapper.eq(auditStatus != null, ResearchProject::getAuditStatus, auditStatus)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(ResearchProject::getProjectName, keyword)
                        .or().like(ResearchProject::getProjectNo, keyword)
                        .or().like(ResearchProject::getProjectSource, keyword)
                        .or().like(ResearchProject::getLeaderName, keyword))
                .orderByDesc(ResearchProject::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public ResearchProject getProjectById(Long id, LoginUser loginUser) {
        ResearchProject project = getById(id);
        if (project == null) {
            return null;
        }
        ensureReadable(project.getStudentId(), loginUser);
        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveProject(ResearchProject project, LoginUser loginUser) {
        if (loginUser == null) {
            throw new BusinessException("当前登录用户不存在");
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = requireStudentByUserId(loginUser.getUserId());
            project.setStudentId(currentStudent.getId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            requireStudentById(project.getStudentId());
        } else {
            throw new BusinessException("无权新增项目成果");
        }

        if (project.getAuditStatus() == null) {
            project.setAuditStatus(0);
        }
        if (project.getScore() == null) {
            project.setScore(BigDecimal.ZERO);
        }
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
            StudentInfo currentStudent = requireStudentByUserId(loginUser.getUserId());
            if (!existing.getStudentId().equals(currentStudent.getId())) {
                log.warn("用户 {} 尝试更新不属于自己的项目 {}", loginUser.getUserId(), project.getId());
                throw new BusinessException("无权更新该项目成果");
            }
            project.setStudentId(existing.getStudentId());
        } else if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
            if (project.getStudentId() == null) {
                project.setStudentId(existing.getStudentId());
            } else {
                requireStudentById(project.getStudentId());
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

    private void applyDataScope(LambdaQueryWrapper<ResearchProject> wrapper, Long requestedStudentId, LoginUser loginUser) {
        if (loginUser == null || UserTypeEnum.isAdmin(loginUser.getUserType())) {
            wrapper.eq(requestedStudentId != null, ResearchProject::getStudentId, requestedStudentId);
            return;
        }

        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo currentStudent = findStudentByUserId(loginUser.getUserId());
            wrapper.eq(ResearchProject::getStudentId, currentStudent == null ? -1L : currentStudent.getId());
            return;
        }

        if (UserTypeEnum.isTutor(loginUser.getUserType())) {
            List<Long> studentIds = listStudentIdsByTutorId(loginUser.getUserId());
            if (studentIds.isEmpty()) {
                wrapper.eq(ResearchProject::getStudentId, -1L);
            } else {
                wrapper.in(ResearchProject::getStudentId, studentIds);
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
                throw new BusinessException("无权查看该项目成果");
            }
            return;
        }
        if (UserTypeEnum.isTutor(loginUser.getUserType()) && listStudentIdsByTutorId(loginUser.getUserId()).contains(ownerStudentId)) {
            return;
        }
        throw new BusinessException("无权查看该项目成果");
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
