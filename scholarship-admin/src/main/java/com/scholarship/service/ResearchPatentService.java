package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.security.LoginUser;

import java.util.List;
import java.util.Map;

/**
 * 科研专利服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface ResearchPatentService extends IService<ResearchPatent> {

    /**
     * 保存专利（带用户权限验证）
     *
     * @param patent 专利信息
     * @param userId 当前用户 ID
     * @return 是否保存成功
     */
    boolean savePatent(ResearchPatent patent, LoginUser loginUser);

    /**
     * 更新专利（带用户权限验证）
     *
     * @param patent 专利信息
     * @param userId 当前用户 ID
     * @return 是否更新成功
     */
    boolean updatePatent(ResearchPatent patent, LoginUser loginUser);

    /**
     * 分页查询专利（带权限过滤）
     *
     * @param page    分页对象
     * @param loginUser 当前登录用户
     * @return 分页结果
     */
    IPage<ResearchPatent> pagePatents(Page<ResearchPatent> page, LoginUser loginUser);

    /**
     * 批量查询学生的审核通过专利
     *
     * @param studentIds 学生 ID 列表
     * @return 按学生 ID 分组的专利列表
     */
    Map<Long, List<ResearchPatent>> mapByStudentIds(List<Long> studentIds);

    /**
     * 审核专利
     *
     * @param id           专利 ID
     * @param auditStatus  审核状态
     * @param auditComment 审核意见
     * @return 是否成功
     */
    boolean audit(Long id, Integer auditStatus, String auditComment);
}
