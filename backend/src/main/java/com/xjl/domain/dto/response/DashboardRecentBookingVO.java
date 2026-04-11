package com.xjl.domain.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DashboardRecentBookingVO {
    private Long id;
    private String roomName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String purpose;
    private String applicantName;
}
