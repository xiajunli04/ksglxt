package com.xjl.domain.dto.response;

import lombok.Data;

@Data
public class DashboardStatsVO {

    // 管理员统计
    private Long totalClassrooms;
    private Long pendingApprovals;
    private Long todayBookings;
    private Long monthBookings;

    // 教师统计
    private Long myBookings;
    private Long approvalRate;
    private Long weekBookings;

    // 学生统计
    private Long myPending;
    private Long myToday;
}
