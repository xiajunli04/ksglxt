package com.xjl.controller;

import com.xjl.domain.PageResult;
import com.xjl.domain.R;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.entity.Notification;
import com.xjl.security.LoginUser;
import com.xjl.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping("/list")
    public R<PageResult<Notification>> myNotifications(@AuthenticationPrincipal LoginUser loginUser,
                                                       PageQuery query) {
        return R.ok(notificationService.myNotifications(loginUser.getUserId(), query));
    }

    @GetMapping("/unread-count")
    public R<Long> getUnreadCount(@AuthenticationPrincipal LoginUser loginUser) {
        return R.ok(notificationService.getUnreadCount(loginUser.getUserId()));
    }

    @PutMapping("/{id}/read")
    public R<Void> markRead(@AuthenticationPrincipal LoginUser loginUser,
                            @PathVariable Long id) {
        notificationService.markRead(loginUser.getUserId(), id);
        return R.ok();
    }

    @PutMapping("/read-all")
    public R<Void> markAllRead(@AuthenticationPrincipal LoginUser loginUser) {
        notificationService.markAllRead(loginUser.getUserId());
        return R.ok();
    }
}
