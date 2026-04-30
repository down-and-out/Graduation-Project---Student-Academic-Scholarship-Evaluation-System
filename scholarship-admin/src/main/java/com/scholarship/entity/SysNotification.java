package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统通知实体类
 * <p>
 * 对应数据库表：sys_notification
 * 存储系统通知消息，用于向用户推送重要信息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Data
@TableName("sys_notification")
@Schema(description = "系统通知")
public class SysNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "通知 ID")
    private Long id;

    /**
     * 通知标题
     */
    @Schema(description = "通知标题")
    private String title;

    /**
     * 通知内容
     */
    @Schema(description = "通知内容")
    private String content;

    /**
     * 通知类型
     * 1-系统通知 2-申请通知 3-评审通知 4-结果通知
     */
    @Schema(description = "通知类型：1-系统 2-申请 3-评审 4-结果")
    private Integer notificationType;

    /**
     * 接收人 ID（为空表示全员通知）
     */
    @Schema(description = "接收人 ID")
    private Long receiverId;

    /**
     * 角色 ID
     */
    @Schema(description = "角色 ID")
    private Long roleId;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 接收人类型
     * 1-全部 2-研究生 3-导师 4-管理员
     */
    @Schema(description = "接收人类型：1-全部 2-研究生 3-导师 4-管理员")
    private Integer receiverType;

    /**
     * 关联业务 ID
     */
    @Schema(description = "关联业务 ID")
    private Long businessId;

    /**
     * 是否已读
     * 0-未读 1-已读
     */
    @Schema(description = "是否已读：0-未读 1-已读")
    private Integer isRead;

    /**
     * 阅读时间
     */
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    /**
     * 发送人 ID
     */
    @Schema(description = "发送人 ID")
    private Long senderId;

    /**
     * 发送人姓名
     */
    @Schema(description = "发送人姓名")
    private String senderName;

    /**
     * 逻辑删除标记
     * 0-未删除 1-已删除
     */
    @TableLogic
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
