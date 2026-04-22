package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.entity.ApplicationAchievement;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.mapper.ApplicationAchievementMapper;
import com.scholarship.mapper.ScholarshipApplicationMapper;
import com.scholarship.service.ApplicationAchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 申请成果关联服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationAchievementServiceImpl
        extends ServiceImpl<ApplicationAchievementMapper, ApplicationAchievement>
        implements ApplicationAchievementService {

    private final ScholarshipApplicationMapper scholarshipApplicationMapper;

    @Override
    public List<ApplicationAchievement> listByApplicationId(Long applicationId) {
        return list(new LambdaQueryWrapper<ApplicationAchievement>()
                .eq(ApplicationAchievement::getApplicationId, applicationId)
                .orderByAsc(ApplicationAchievement::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceByApplicationId(Long applicationId, List<ApplicationAchievement> achievements) {
        validateApplicationExists(applicationId);

        removeByApplicationId(applicationId);

        if (achievements == null || achievements.isEmpty()) {
            return true;
        }

        achievements.forEach(item -> {
            item.setId(null);
            item.setApplicationId(applicationId);
            if (item.getVersion() == null) {
                item.setVersion(1);
            }
        });

        return saveBatch(achievements);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByApplicationId(Long applicationId) {
        return remove(new LambdaQueryWrapper<ApplicationAchievement>()
                .eq(ApplicationAchievement::getApplicationId, applicationId));
    }

    private void validateApplicationExists(Long applicationId) {
        ScholarshipApplication application = scholarshipApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请记录不存在");
        }
    }
}
