package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.exception.BusinessException;
import com.scholarship.mapper.ResearchPatentMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchPatentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResearchPatentServiceImpl extends ServiceImpl<ResearchPatentMapper, ResearchPatent> implements ResearchPatentService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePatent(ResearchPatent patent, LoginUser loginUser) {
        if (loginUser == null) {
            return false;
        }

        if (patent.getStudentId() == null) {
            if (Integer.valueOf(1).equals(loginUser.getUserType())) {
                patent.setStudentId(loginUser.getUserId());
            } else {
                return false;
            }
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
            return false;
        }

        ResearchPatent existing = getById(patent.getId());
        if (existing == null) {
            return false;
        }

        boolean isAdmin = Integer.valueOf(3).equals(loginUser.getUserType());
        boolean isOwner = existing.getStudentId().equals(loginUser.getUserId());
        if (!isAdmin && !isOwner) {
            log.warn("用户 {} 尝试更新不属于自己的专利 {}", loginUser.getUserId(), patent.getId());
            return false;
        }

        if (!isAdmin) {
            patent.setStudentId(existing.getStudentId());
        }

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

        if (Integer.valueOf(1).equals(loginUser.getUserType())) {
            return lambdaQuery()
                    .eq(ResearchPatent::getStudentId, loginUser.getUserId())
                    .page(page);
        }

        return page(page);
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
}
