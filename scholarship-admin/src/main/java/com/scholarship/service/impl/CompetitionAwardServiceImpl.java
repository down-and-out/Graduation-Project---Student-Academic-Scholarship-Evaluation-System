package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.CompetitionAward;
import com.scholarship.mapper.CompetitionAwardMapper;
import com.scholarship.service.CompetitionAwardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学科竞赛获奖服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class CompetitionAwardServiceImpl extends ServiceImpl<CompetitionAwardMapper, CompetitionAward> implements CompetitionAwardService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAward(CompetitionAward award, Long userId) {
        // 设置学生 ID（如果未设置）
        if (award.getStudentId() == null) {
            award.setStudentId(userId);
        }
        // 初始化审核状态为待审核
        if (award.getAuditStatus() == null) {
            award.setAuditStatus(0);
        }
        // 初始化分数
        if (award.getScore() == null) {
            award.setScore(java.math.BigDecimal.ZERO);
        }
        return save(award);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAward(CompetitionAward award, Long userId) {
        CompetitionAward existing = getById(award.getId());
        if (existing == null) {
            return false;
        }
        // 权限验证：只有获奖所有者或管理员可以更新
        if (!existing.getStudentId().equals(userId)) {
            log.warn("用户 {} 尝试更新不属于她的竞赛获奖 {}", userId, award.getId());
            return false;
        }
        // 保留审核相关字段（防止用户恶意修改）
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
            .eq(CompetitionAward::getAuditStatus, 1)); // 审核通过

        return awards.stream()
            .collect(Collectors.groupingBy(CompetitionAward::getStudentId));
    }
}
