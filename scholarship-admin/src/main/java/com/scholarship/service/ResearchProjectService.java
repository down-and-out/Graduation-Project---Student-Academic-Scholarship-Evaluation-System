package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ResearchProject;
import com.scholarship.security.LoginUser;

import java.util.List;
import java.util.Map;

/**
 * 科研项目服务接口。
 */
public interface ResearchProjectService extends IService<ResearchProject> {

    /**
     * 保存项目，并根据当前登录用户补齐学生档案 ID。
     */
    boolean saveProject(ResearchProject project, LoginUser loginUser);

    /**
     * 更新项目，并校验当前用户是否有权操作该学生成果。
     */
    boolean updateProject(ResearchProject project, LoginUser loginUser);

    /**
     * 分页查询项目，并按角色应用数据边界。
     */
    IPage<ResearchProject> pageProjects(Page<ResearchProject> page, Long studentId, Integer auditStatus,
                                        String keyword, LoginUser loginUser);

    /**
     * 按角色数据边界获取项目详情。
     */
    ResearchProject getProjectById(Long id, LoginUser loginUser);

    /**
     * 批量查询学生的审核通过项目。
     */
    Map<Long, List<ResearchProject>> mapByStudentIds(List<Long> studentIds);
}
