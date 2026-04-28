package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.enums.OperationLogTypeEnum;
import com.scholarship.common.result.Result;
import com.scholarship.common.util.ParamParserUtil;
import com.scholarship.dto.query.OperationLogQuery;
import com.scholarship.entity.SysOperationLog;
import com.scholarship.service.SysOperationLogService;
import com.scholarship.vo.OperationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/operation-log")
@RequiredArgsConstructor
@Tag(name = "操作日志", description = "系统操作日志查询接口")
public class SysOperationLogController {

    private final SysOperationLogService sysOperationLogService;

    @GetMapping("/page")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "分页查询操作日志", description = "支持按操作类型和操作人筛选，操作类型兼容单个或多个值")
    public Result<IPage<OperationLogVO>> page(
            @Parameter(description = "当前页", example = "1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "操作类型，支持单个或多个值", example = "1,2")
            @RequestParam(required = false) List<String> operationType,
            @Parameter(description = "操作人用户名", example = "admin")
            @RequestParam(required = false) String username) {

        OperationLogQuery query = new OperationLogQuery();
        query.setCurrent(current);
        query.setSize(size);

        List<Integer> operationTypes = ParamParserUtil.parseIntegerParams(operationType);
        if (!operationTypes.isEmpty()) {
            query.setOperationTypes(operationTypes);
            query.setOperationType(operationTypes.size() == 1 ? operationTypes.get(0) : null);
        }

        query.setKeyword(username);
        IPage<SysOperationLog> result = sysOperationLogService.queryPage(query);
        return Result.success(result.convert(this::toOperationLogVO));
    }

    private OperationLogVO toOperationLogVO(SysOperationLog logRecord) {
        OperationLogVO view = new OperationLogVO();
        view.setId(logRecord.getId());
        view.setOperatorId(logRecord.getOperatorId());
        view.setOperatorName(logRecord.getOperatorName());
        view.setOperationType(logRecord.getOperationType());
        view.setOperationTypeLabel(resolveOperationTypeLabel(logRecord.getOperationType()));
        view.setDescription(logRecord.getDescription());
        view.setOperatorIp(logRecord.getOperatorIp());
        view.setCreateTime(logRecord.getCreateTime());
        return view;
    }

    private String resolveOperationTypeLabel(Integer operationType) {
        String label = OperationLogTypeEnum.getLabelByCode(operationType);
        if (label != null) {
            return label;
        }
        return operationType == null ? "" : String.valueOf(operationType);
    }
}
