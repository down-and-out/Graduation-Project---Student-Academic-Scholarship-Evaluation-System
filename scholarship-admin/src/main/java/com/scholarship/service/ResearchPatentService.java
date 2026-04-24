package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.security.LoginUser;

import java.util.List;
import java.util.Map;

/**
 * 科研专利服务接口。
 */
public interface ResearchPatentService extends IService<ResearchPatent> {

    /**
     * 保存专利，并根据当前登录用户补齐学生档案 ID。
     */
    boolean savePatent(ResearchPatent patent, LoginUser loginUser);

    /**
     * 更新专利，并校验当前用户是否有权操作该学生成果。
     */
    boolean updatePatent(ResearchPatent patent, LoginUser loginUser);

    /**
     * 分页查询专利，并按角色应用数据边界。
     */
    IPage<ResearchPatent> pagePatents(Page<ResearchPatent> page, Long studentId, Integer auditStatus,
                                      String keyword, LoginUser loginUser);

    /**
     * 按角色数据边界获取专利详情。
     */
    ResearchPatent getPatentById(Long id, LoginUser loginUser);

    /**
     * 批量查询学生的审核通过专利。
     */
    Map<Long, List<ResearchPatent>> mapByStudentIds(List<Long> studentIds);

    /**
     * 审核专利。
     */
    boolean audit(Long id, Integer auditStatus, String auditComment);
}
