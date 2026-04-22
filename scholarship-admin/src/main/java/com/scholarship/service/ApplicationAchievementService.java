package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ApplicationAchievement;

import java.util.List;

/**
 * 申请成果关联服务接口
 */
public interface ApplicationAchievementService extends IService<ApplicationAchievement> {

    /**
     * 按申请 ID 查询成果关联列表
     *
     * @param applicationId 申请 ID
     * @return 成果关联列表
     */
    List<ApplicationAchievement> listByApplicationId(Long applicationId);

    /**
     * 覆盖保存某个申请的成果关联列表
     *
     * @param applicationId 申请 ID
     * @param achievements 成果关联列表
     * @return 是否保存成功
     */
    boolean replaceByApplicationId(Long applicationId, List<ApplicationAchievement> achievements);

    /**
     * 删除某个申请下的全部成果关联
     *
     * @param applicationId 申请 ID
     * @return 是否删除成功
     */
    boolean removeByApplicationId(Long applicationId);
}
