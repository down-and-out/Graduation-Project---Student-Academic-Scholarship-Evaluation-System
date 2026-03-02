package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ResearchProject;

/**
 * 科研项目服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface ResearchProjectService extends IService<ResearchProject> {

    /**
     * 保存项目（带用户权限验证）
     *
     * @param project 项目信息
     * @param userId  当前用户 ID
     * @return 是否保存成功
     */
    boolean saveProject(ResearchProject project, Long userId);

    /**
     * 更新项目（带用户权限验证）
     *
     * @param project 项目信息
     * @param userId  当前用户 ID
     * @return 是否更新成功
     */
    boolean updateProject(ResearchProject project, Long userId);
}
