package com.xjl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.entity.Notification;
import com.xjl.mapper.NotificationMapper;
import com.xjl.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

    @Override
    public PageResult<Notification> myNotifications(Long userId, PageQuery query) {
        Page<Notification> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        Page<Notification> result = page(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    public void markRead(Long userId, Long id) {
        Notification notification = getById(id);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此通知");
        }
        Notification update = new Notification();
        update.setId(id);
        update.setIsRead("1");
        updateById(update);
    }

    @Override
    public void markAllRead(Long userId) {
        List<Notification> unread = list(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, "0")
        );
        unread.forEach(n -> {
            n.setIsRead("1");
            updateById(n);
        });
    }

    @Override
    public long getUnreadCount(Long userId) {
        return count(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, "0")
        );
    }
}
