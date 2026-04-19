package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.SysNotification;
import com.scholarship.mapper.SysNotificationMapper;
import com.scholarship.service.SysNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统通知服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Slf4j
@Service
public class SysNotificationServiceImpl extends ServiceImpl<SysNotificationMapper, SysNotification>
        implements SysNotificationService {

    @Override
    public IPage<SysNotification> pageNotifications(Long current, Long size, Long userId, Integer isRead, Integer notificationType) {
        log.debug("分页查询通知，current={}, size={}, userId={}, isRead={}, type={}", current, size, userId, isRead, notificationType);

        Page<SysNotification> page = new Page<>(current, size);
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();

        // 查询发给该用户的通知或全员通知
        wrapper.and(w -> w.eq(SysNotification::getReceiverId, userId)
                .or().eq(SysNotification::getReceiverType, 1));

        if (isRead != null) {
            wrapper.eq(SysNotification::getIsRead, isRead);
        }
        if (notificationType != null) {
            wrapper.eq(SysNotification::getNotificationType, notificationType);
        }

        wrapper.orderByDesc(SysNotification::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendNotification(SysNotification notification) {
        log.info("发送通知，title={}, receiverId={}, type={}", notification.getTitle(), notification.getReceiverId(), notification.getNotificationType());

        notification.setIsRead(0);
        return save(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long id, Long userId) {
        log.debug("标记通知为已读，id={}, userId={}", id, userId);

        SysNotification notification = getById(id);
        if (notification == null) {
            return false;
        }

        // 只允许标记属于自己的通知
        if (notification.getReceiverId() != null && !notification.getReceiverId().equals(userId)) {
            return false;
        }

        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        return updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markBatchAsRead(List<Long> ids, Long userId) {
        log.info("批量标记通知为已读，ids={}, userId={}", ids, userId);

        for (Long id : ids) {
            markAsRead(id, userId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotification(Long id) {
        log.info("删除通知，id={}", id);
        return removeById(id);
    }

    @Override
    public int getUnreadCount(Long userId) {
        log.debug("获取未读通知数，userId={}", userId);

        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(SysNotification::getReceiverId, userId)
                    .or().eq(SysNotification::getReceiverType, 1))
            .eq(SysNotification::getIsRead, 0);
        Long count = count(wrapper);

        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<SysNotification> getLatestNotifications(Long userId, Integer limit) {
        log.debug("获取最新通知，userId={}, limit={}", userId, limit);

        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(SysNotification::getReceiverId, userId)
                    .or().eq(SysNotification::getReceiverType, 1))
            .orderByDesc(SysNotification::getCreateTime)
            .last("LIMIT " + (limit != null ? limit : 10));
        return list(wrapper);
    }
}
