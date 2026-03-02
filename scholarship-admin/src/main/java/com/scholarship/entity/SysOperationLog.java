package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作日志实体类
 * <p>
 * 对应数据库表：sys_operation_log
 * 记录用户的重要操作日志，用于审计和追溯
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Data
@TableName("sys_operation_log")
@Schema(description = "系统操作日志")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "日志 ID")
    private Long id;

    /**
     * 操作人 ID
     */
    @Schema(description = "操作人 ID")
    @TableField("user_id")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名")
    @TableField("username")
    private String operatorName;

    /**
     * 操作模块
     */
    @Schema(description = "操作模块")
    private String module;

    /**
     * 操作类型
     * 1-查询 2-新增 3-修改 4-删除 5-审核 6-导出
     */
    @Schema(description = "操作类型：1-查询 2-新增 3-修改 4-删除 5-审核 6-导出")
    @TableField("operation_type")
    private Integer operationType;

    /**
     * 操作描述
     */
    @Schema(description = "操作描述")
    @TableField("operation_desc")
    private String description;

    /**
     * 请求方法
     */
    @Schema(description = "请求方法")
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求 URL
     */
    @Schema(description = "请求 URL")
    @TableField("request_uri")
    private String requestUrl;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String requestParams;

    /**
     * 返回结果
     */
    @Schema(description = "返回结果")
    private String responseData;

    /**
     * 操作状态
     * 1-成功 2-失败
     */
    @Schema(description = "操作状态：1-成功 2-失败")
    private Integer status;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 执行时长（毫秒）
     */
    @Schema(description = "执行时长（毫秒）")
    @TableField("response_time")
    private Long executionTime;

    /**
     * 操作 IP
     */
    @Schema(description = "操作 IP")
    @TableField("ip_address")
    private String operatorIp;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
