package com.scholarship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.ResearchProject;
import com.scholarship.mapper.ResearchProjectMapper;
import com.scholarship.service.ResearchProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 科研项目服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class ResearchProjectServiceImpl extends ServiceImpl<ResearchProjectMapper, ResearchProject> implements ResearchProjectService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveProject(ResearchProject project, Long userId) {
        // 设置学生 ID（如果未设置）
        if (project.getStudentId() == null) {
            project.setStudentId(userId);
        }
        // 初始化审核状态为待审核
        if (project.getAuditStatus() == null) {
            project.setAuditStatus(0);
        }
        // 初始化分数
        if (project.getScore() == null) {
            project.setScore(java.math.BigDecimal.ZERO);
        }
        return save(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProject(ResearchProject project, Long userId) {
        ResearchProject existing = getById(project.getId());
        if (existing == null) {
            return false;
        }
        // 权限验证：只有项目所有者或管理员可以更新
        if (!existing.getStudentId().equals(userId)) {
            log.warn("用户 {} 尝试更新不属于她的项目 {}", userId, project.getId());
            return false;
        }
        // 保留审核相关字段（防止用户恶意修改）
        project.setAuditStatus(existing.getAuditStatus());
        project.setAuditorId(existing.getAuditorId());
        project.setAuditTime(existing.getAuditTime());
        project.setAuditComment(existing.getAuditComment());
        project.setScore(existing.getScore());
        return updateById(project);
    }
}
