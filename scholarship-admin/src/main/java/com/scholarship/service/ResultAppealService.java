package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ResultAppeal;
import com.scholarship.security.LoginUser;

/**
 * 结果异议服务接口。
 */
public interface ResultAppealService extends IService<ResultAppeal> {

    /**
     * 学生提交异议。
     */
    boolean submitAppeal(ResultAppeal appeal, LoginUser loginUser);

    /**
     * 管理员处理异议。
     */
    boolean handleAppeal(Long id, String handleOpinion, LoginUser loginUser);
}
