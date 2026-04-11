package com.xjl.domain.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class BookingVO {

    private Long id;

    private Long applicantId;

    private Long classroomId;

    private LocalDate bookingDate;

    private Long timeSlotId;

    private LocalTime startTime;

    private LocalTime endTime;

    private String purpose;

    private Integer expectedAttendance;

    private String contactPhone;

    private String attachmentUrl;

    private String status;

    private String statusText;

    private String rejectReason;

    private Long approverId;

    private LocalDateTime approveTime;

    private LocalDateTime createTime;

    private String applicantName;

    private String classroomName;

    private String timeSlotName;
}
