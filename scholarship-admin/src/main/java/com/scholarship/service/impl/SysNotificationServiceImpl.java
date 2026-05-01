package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.SysNotification;
import com.scholarship.entity.SysRole;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.mapper.SysNotificationMapper;
import com.scholarship.mapper.SysRoleMapper;
import com.scholarship.service.SysNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 系统通知服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysNotificationServiceImpl extends ServiceImpl<SysNotificationMapper, SysNotification>
        implements SysNotificationService {

    private static final int RECEIVER_TYPE_ALL = 1;
    private static final int RECEIVER_TYPE_USER = 2;
    private static final int RECEIVER_TYPE_ROLE = 3;

    private final SysRoleMapper sysRoleMapper;

    @Lazy
    @Autowired
    private SysNotificationServiceImpl self;

    @Override
    public IPage<SysNotification> pageNotifications(Long current, Long size, Long userId, Integer userType,
                                                    Integer isRead, Integer notificationType) {
        log.debug("分页查询通知: current={}, size={}, userId={}, userType={}, isRead={}, type={}",
                current, size, userId, userType, isRead, notificationType);

        Page<SysNotification> page = new Page<>(current, size);
        LambdaQueryWrapper<SysNotification> wrapper = buildAccessibleWrapper(userId);

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
        validateNotification(notification);
        log.info("发送通知: title={}, receiverType={}, receiverId={}, roleId={}, type={}",
                notification.getTitle(), notification.getReceiverType(), notification.getReceiverId(),
                notification.getRoleId(), notification.getNotificationType());

        notification.setIsRead(0);
        if (notification.getVersion() == null) {
            notification.setVersion(1);
        }
        return save(notification);
    }

    @Async
    @Override
    public void sendNotificationAsync(SysNotification notification) {
        try {
            self.sendNotification(notification);
        } catch (Exception e) {
            log.warn("Async notification send failed: title={}", notification.getTitle(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long id, Long userId, Integer userType) {
        log.debug("标记通知为已读: id={}, userId={}", id, userId);

        SysNotification notification = getAccessibleNotification(id, userId, userType);
        if (notification == null) {
            return false;
        }

        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        return updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markBatchAsRead(List<Long> ids, Long userId, Integer userType) {
        log.info("批量标记通知为已读: ids={}, userId={}", ids, userId);

        if (ids == null || ids.isEmpty()) {
            return true;
        }

        LambdaUpdateWrapper<SysNotification> wrapper = new LambdaUpdateWrapper<SysNotification>()
                .in(SysNotification::getId, ids)
                .set(SysNotification::getIsRead, 1)
                .set(SysNotification::getReadTime, LocalDateTime.now());

        applyAccessibleConditions(wrapper, userId);
        return update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotification(Long id) {
        log.info("删除通知: id={}", id);
        return removeById(id);
    }

    @Override
    public int getUnreadCount(Long userId, Integer userType) {
        Long count = count(buildAccessibleWrapper(userId).eq(SysNotification::getIsRead, 0));
        return count == null ? 0 : count.intValue();
    }

    @Override
    public List<SysNotification> getLatestNotifications(Long userId, Integer userType, Integer limit) {
        LambdaQueryWrapper<SysNotification> wrapper = buildAccessibleWrapper(userId)
                .orderByDesc(SysNotification::getCreateTime)
                .last("LIMIT " + (limit != null ? limit : 10));
        return list(wrapper);
    }

    @Override
    public SysNotification getAccessibleNotification(Long id, Long userId, Integer userType) {
        LambdaQueryWrapper<SysNotification> wrapper = buildAccessibleWrapper(userId)
                .eq(SysNotification::getId, id)
                .last("LIMIT 1");
        return getOne(wrapper, false);
    }

    private LambdaQueryWrapper<SysNotification> buildAccessibleWrapper(Long userId) {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        List<Long> roleIds = getUserRoleIds(userId);

        wrapper.and(group -> {
            group.eq(SysNotification::getReceiverType, RECEIVER_TYPE_ALL)
                    .or(child -> child
                            .eq(SysNotification::getReceiverType, RECEIVER_TYPE_USER)
                            .eq(SysNotification::getReceiverId, userId));

            if (!roleIds.isEmpty()) {
                group.or(child -> child
                        .eq(SysNotification::getReceiverType, RECEIVER_TYPE_ROLE)
                        .in(SysNotification::getRoleId, roleIds));
            }
        });
        return wrapper;
    }

    private void applyAccessibleConditions(LambdaUpdateWrapper<SysNotification> wrapper, Long userId) {
        List<Long> roleIds = getUserRoleIds(userId);

        wrapper.and(group -> {
            group.eq(SysNotification::getReceiverType, RECEIVER_TYPE_ALL)
                    .or(child -> child
                            .eq(SysNotification::getReceiverType, RECEIVER_TYPE_USER)
                            .eq(SysNotification::getReceiverId, userId));

            if (!roleIds.isEmpty()) {
                group.or(child -> child
                        .eq(SysNotification::getReceiverType, RECEIVER_TYPE_ROLE)
                        .in(SysNotification::getRoleId, roleIds));
            }
        });
    }

    private List<Long> getUserRoleIds(Long userId) {
        return sysRoleMapper.selectRolesByUserId(userId).stream()
                .map(SysRole::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private void validateNotification(SysNotification notification) {
        if (notification == null) {
            throw new BusinessException("通知内容不能为空");
        }
        Integer receiverType = notification.getReceiverType();
        if (receiverType == null) {
            throw new BusinessException("通知接收类型不能为空");
        }
        if (receiverType == RECEIVER_TYPE_USER && notification.getReceiverId() == null) {
            throw new BusinessException("指定用户通知必须填写接收者 ID");
        }
        if (receiverType == RECEIVER_TYPE_ROLE && notification.getRoleId() == null) {
            throw new BusinessException("指定角色通知必须填写角色 ID");
        }
        if (receiverType != RECEIVER_TYPE_ALL
                && receiverType != RECEIVER_TYPE_USER
                && receiverType != RECEIVER_TYPE_ROLE) {
            throw new BusinessException("无效的通知接收类型");
        }
    }
}
