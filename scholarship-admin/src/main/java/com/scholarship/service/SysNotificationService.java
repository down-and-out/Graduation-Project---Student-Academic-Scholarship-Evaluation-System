package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.SysNotification;

/**
 * 系统通知服务接口
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
public interface SysNotificationService extends IService<SysNotification> {

    /**
     * 分页查询通知
     *
     * @param current 当前页
     * @param size 每页大小
     * @param userId 用户 ID
     * @param isRead 是否已读
     * @param notificationType 通知类型
     * @return 分页结果
     */
    IPage<SysNotification> pageNotifications(Long current, Long size, Long userId, Integer isRead, Integer notificationType);

    /**
     * 发送通知
     *
     * @param notification 通知信息
     * @return 是否成功
     */
    boolean sendNotification(SysNotification notification);

    /**
     * 标记为已读
     *
     * @param id 通知 ID
     * @param userId 用户 ID
     * @return 是否成功
     */
    boolean markAsRead(Long id, Long userId);

    /**
     * 批量标记为已读
     *
     * @param ids 通知 ID 列表
     * @param userId 用户 ID
     * @return 是否成功
     */
    boolean markBatchAsRead(java.util.List<Long> ids, Long userId);

    /**
     * 删除通知
     *
     * @param id 通知 ID
     * @return 是否成功
     */
    boolean deleteNotification(Long id);

    /**
     * 获取未读通知数
     *
     * @param userId 用户 ID
     * @return 未读通知数
     */
    int getUnreadCount(Long userId);

    /**
     * 获取用户的最新通知
     *
     * @param userId 用户 ID
     * @param limit 数量限制
     * @return 通知列表
     */
    java.util.List<SysNotification> getLatestNotifications(Long userId, Integer limit);
}
