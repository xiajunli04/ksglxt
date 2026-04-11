package com.xjl.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "教室不能为空")
    private Long classroomId;

    @NotBlank(message = "预约日期不能为空")
    private String bookingDate;

    @NotNull(message = "时间段不能为空")
    private Long timeSlotId;

    private String purpose;

    private Integer expectedAttendance;

    private String contactPhone;

    private String attachmentUrl;
}
