package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ResultAppeal;

/**
 * 结果异议服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface ResultAppealService extends IService<ResultAppeal> {

    /**
     * 提交异议
     *
     * @param appeal 异议信息
     * @return 是否成功
     */
    boolean submitAppeal(ResultAppeal appeal);

    /**
     * 处理异议
     *
     * @param id 异议 ID
     * @param handleResult 处理结果
     * @return 是否成功
     */
    boolean handleAppeal(Long id, String handleResult);
}
