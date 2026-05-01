package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.SysNotification;

import java.util.List;

/**
 * 系统通知服务接口。
 */
public interface SysNotificationService extends IService<SysNotification> {

    IPage<SysNotification> pageNotifications(Long current, Long size, Long userId, Integer userType,
                                             Integer isRead, Integer notificationType);

    boolean sendNotification(SysNotification notification);

    /**
     * 异步发送通知（不阻塞调用线程）。
     *
     * @param notification 通知实体
     */
    void sendNotificationAsync(SysNotification notification);

    boolean markAsRead(Long id, Long userId, Integer userType);

    boolean markBatchAsRead(List<Long> ids, Long userId, Integer userType);

    boolean deleteNotification(Long id);

    int getUnreadCount(Long userId, Integer userType);

    List<SysNotification> getLatestNotifications(Long userId, Integer userType, Integer limit);

    SysNotification getAccessibleNotification(Long id, Long userId, Integer userType);
}
