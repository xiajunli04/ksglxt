package com.xjl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.entity.Notification;

public interface INotificationService extends IService<Notification> {

    PageResult<Notification> myNotifications(Long userId, PageQuery query);

    void markRead(Long userId, Long id);

    void markAllRead(Long userId);

    long getUnreadCount(Long userId);
}
