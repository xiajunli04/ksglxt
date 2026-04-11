package com.xjl.service;

import com.xjl.domain.dto.response.*;

import java.util.List;

public interface IDashboardService {

    DashboardStatsVO getStats(Long userId, String roleCode);

    List<DashboardAnnouncementVO> getRecentAnnouncements();

    List<DashboardHotClassroomVO> getHotClassrooms();

    List<DashboardAvailableClassroomVO> getAvailableClassrooms();

    List<DashboardRecentBookingVO> getRecentBookings(Long userId, String roleCode);
}
