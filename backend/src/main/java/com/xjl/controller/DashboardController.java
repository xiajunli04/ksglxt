package com.xjl.controller;

import com.xjl.domain.R;
import com.xjl.domain.dto.response.*;
import com.xjl.security.LoginUser;
import com.xjl.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/stats")
    public R<DashboardStatsVO> getStats(@AuthenticationPrincipal LoginUser loginUser) {
        String roleCode = getRoleCode(loginUser);
        return R.ok(dashboardService.getStats(loginUser.getUserId(), roleCode));
    }

    @GetMapping("/recent-announcements")
    public R<List<DashboardAnnouncementVO>> getRecentAnnouncements() {
        return R.ok(dashboardService.getRecentAnnouncements());
    }

    @GetMapping("/hot-classrooms")
    public R<List<DashboardHotClassroomVO>> getHotClassrooms() {
        return R.ok(dashboardService.getHotClassrooms());
    }

    @GetMapping("/available-classrooms")
    public R<List<DashboardAvailableClassroomVO>> getAvailableClassrooms() {
        return R.ok(dashboardService.getAvailableClassrooms());
    }

    @GetMapping("/recent-bookings")
    public R<List<DashboardRecentBookingVO>> getRecentBookings(@AuthenticationPrincipal LoginUser loginUser) {
        String roleCode = getRoleCode(loginUser);
        return R.ok(dashboardService.getRecentBookings(loginUser.getUserId(), roleCode));
    }

    private String getRoleCode(LoginUser loginUser) {
        if (loginUser.getPermissions().contains("*:*:*") || loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN"))) {
            return "ADMIN";
        }
        if (loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("TEACHER"))) {
            return "TEACHER";
        }
        return "STUDENT";
    }
}
