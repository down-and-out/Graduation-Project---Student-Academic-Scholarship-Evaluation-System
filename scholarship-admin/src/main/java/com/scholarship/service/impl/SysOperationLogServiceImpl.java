package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.dto.query.OperationLogQuery;
import com.scholarship.entity.SysOperationLog;
import com.scholarship.mapper.SysOperationLogMapper;
import com.scholarship.service.SysOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 系统操作日志服务实现
 */
@Slf4j
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    @Override
    public IPage<SysOperationLog> queryPage(OperationLogQuery query) {
        log.debug("分页查询操作日志，query={}", query);

        Page<SysOperationLog> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();

        // 操作类型筛选
        if (StringUtils.isNotBlank(query.getOperationType())) {
            wrapper.eq(SysOperationLog::getOperationType, query.getOperationType());
        }

        // 操作人筛选（模糊查询）
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(SysOperationLog::getOperatorName, query.getKeyword());
        }

        // 模块筛选
        if (StringUtils.isNotBlank(query.getModule())) {
            wrapper.eq(SysOperationLog::getModule, query.getModule());
        }

        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(SysOperationLog::getStatus, query.getStatus());
        }

        // 用户 ID 筛选
        if (query.getUserId() != null) {
            wrapper.eq(SysOperationLog::getOperatorId, query.getUserId());
        }

        // 按时间降序排列
        wrapper.orderByDesc(SysOperationLog::getCreateTime);

        return page(page, wrapper);
    }
}
