package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.CompetitionAward;

/**
 * 学科竞赛获奖服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface CompetitionAwardService extends IService<CompetitionAward> {

    /**
     * 保存竞赛获奖（带用户权限验证）
     *
     * @param award  获奖信息
     * @param userId 当前用户 ID
     * @return 是否保存成功
     */
    boolean saveAward(CompetitionAward award, Long userId);

    /**
     * 更新竞赛获奖（带用户权限验证）
     *
     * @param award  获奖信息
     * @param userId 当前用户 ID
     * @return 是否更新成功
     */
    boolean updateAward(CompetitionAward award, Long userId);
}
