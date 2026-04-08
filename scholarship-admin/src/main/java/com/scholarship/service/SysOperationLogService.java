package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.dto.query.OperationLogQuery;
import com.scholarship.entity.SysOperationLog;

/**
 * 系统操作日志服务接口
 */
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 分页查询操作日志
     *
     * @param query 查询参数
     * @return 分页结果
     */
    IPage<SysOperationLog> queryPage(OperationLogQuery query);
}
