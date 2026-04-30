package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作日志实体。
 */
@Data
@TableName("sys_operation_log")
@Schema(description = "系统操作日志")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "日志 ID")
    private Long id;

    @TableField("operator_id")
    @Schema(description = "操作人 ID")
    private Long operatorId;

    @TableField("operator_name")
    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "所属模块")
    private String module;

    @TableField("operation_type")
    @Schema(description = "操作类型")
    private Integer operationType;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "操作动作")
    private String operation;

    @TableField("method")
    @Schema(description = "请求方法")
    private String requestMethod;

    @TableField("params")
    @Schema(description = "请求参数")
    private String requestParams;

    @TableField("request_url")
    @Schema(description = "请求 URL")
    private String requestUrl;

    @TableField("response_data")
    @Schema(description = "响应结果")
    private String responseData;

    @TableField("operator_ip")
    @Schema(description = "操作 IP")
    private String operatorIp;

    @Schema(description = "归属地区")
    private String location;

    @Schema(description = "浏览器")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "执行状态")
    private Integer status;

    @TableField("error_msg")
    @Schema(description = "错误信息")
    private String errorMsg;

    @TableField("execution_time")
    @Schema(description = "执行耗时（毫秒）")
    private Long executionTime;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "版本号")
    private Integer version;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
