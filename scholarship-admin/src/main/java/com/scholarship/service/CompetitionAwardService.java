package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.CompetitionAward;
import com.scholarship.security.LoginUser;

import java.util.List;
import java.util.Map;

/**
 * 学科竞赛获奖服务接口。
 */
public interface CompetitionAwardService extends IService<CompetitionAward> {

    /**
     * 保存竞赛获奖，并根据当前登录用户补齐学生档案 ID。
     */
    boolean saveAward(CompetitionAward award, LoginUser loginUser);

    /**
     * 更新竞赛获奖，并校验当前用户是否有权操作该学生成果。
     */
    boolean updateAward(CompetitionAward award, LoginUser loginUser);

    /**
     * 分页查询竞赛获奖，并按角色应用数据边界。
     */
    IPage<CompetitionAward> pageAwards(Page<CompetitionAward> page, Long studentId, Integer auditStatus,
                                       String keyword, LoginUser loginUser);

    /**
     * 按角色数据边界获取竞赛获奖详情。
     */
    CompetitionAward getAwardById(Long id, LoginUser loginUser);

    /**
     * 批量查询学生的审核通过获奖记录。
     */
    Map<Long, List<CompetitionAward>> mapByStudentIds(List<Long> studentIds);
}
