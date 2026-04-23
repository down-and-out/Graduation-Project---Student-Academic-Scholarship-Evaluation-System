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

import java.util.List;

@Slf4j
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    @Override
    public IPage<SysOperationLog> queryPage(OperationLogQuery query) {
        log.debug("分页查询操作日志: query={}", query);

        Page<SysOperationLog> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();

        List<Integer> operationTypes = query.getOperationTypes();
        if (operationTypes != null && !operationTypes.isEmpty()) {
            if (operationTypes.size() == 1) {
                wrapper.eq(SysOperationLog::getOperationType, operationTypes.get(0));
            } else {
                wrapper.in(SysOperationLog::getOperationType, operationTypes);
            }
        } else if (query.getOperationType() != null) {
            wrapper.eq(SysOperationLog::getOperationType, query.getOperationType());
        }

        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(SysOperationLog::getOperatorName, query.getKeyword());
        }

        if (StringUtils.isNotBlank(query.getModule())) {
            wrapper.eq(SysOperationLog::getModule, query.getModule());
        }

        if (query.getStatus() != null) {
            wrapper.eq(SysOperationLog::getStatus, query.getStatus());
        }

        if (query.getUserId() != null) {
            wrapper.eq(SysOperationLog::getOperatorId, query.getUserId());
        }

        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        return page(page, wrapper);
    }
}
