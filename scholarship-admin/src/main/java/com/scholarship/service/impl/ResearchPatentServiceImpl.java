package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.mapper.ResearchPatentMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchPatentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 科研专利服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class ResearchPatentServiceImpl extends ServiceImpl<ResearchPatentMapper, ResearchPatent> implements ResearchPatentService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePatent(ResearchPatent patent, Long userId) {
        // 设置学生 ID（如果未设置）
        if (patent.getStudentId() == null) {
            patent.setStudentId(userId);
        }
        // 初始化审核状态为待审核
        if (patent.getAuditStatus() == null) {
            patent.setAuditStatus(0);
        }
        // 初始化分数
        if (patent.getScore() == null) {
            patent.setScore(java.math.BigDecimal.ZERO);
        }
        return save(patent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePatent(ResearchPatent patent, Long userId) {
        ResearchPatent existing = getById(patent.getId());
        if (existing == null) {
            return false;
        }
        // 权限验证：只有专利所有者或管理员可以更新
        if (!existing.getStudentId().equals(userId)) {
            log.warn("用户 {} 尝试更新不属于她的专利 {}", userId, patent.getId());
            return false;
        }
        // 保留审核相关字段（防止用户恶意修改）
        patent.setAuditStatus(existing.getAuditStatus());
        patent.setAuditorId(existing.getAuditorId());
        patent.setAuditTime(existing.getAuditTime());
        patent.setAuditComment(existing.getAuditComment());
        patent.setScore(existing.getScore());
        return updateById(patent);
    }

    @Override
    public IPage<ResearchPatent> pagePatents(Page<ResearchPatent> page, LoginUser loginUser) {
        if (loginUser == null) {
            return page(page);
        }
        // 学生只能查看自己的专利
        if (loginUser.getUserType() == 1) {
            return lambdaQuery()
                    .eq(ResearchPatent::getStudentId, loginUser.getUserId())
                    .page(page);
        }
        // 导师和管理员可以查看所有
        return page(page);
    }

    @Override
    public Map<Long, List<ResearchPatent>> mapByStudentIds(List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        List<ResearchPatent> patents = list(new LambdaQueryWrapper<ResearchPatent>()
            .in(ResearchPatent::getStudentId, studentIds)
            .eq(ResearchPatent::getAuditStatus, 1)); // 审核通过

        return patents.stream()
            .collect(Collectors.groupingBy(ResearchPatent::getStudentId));
    }
}
