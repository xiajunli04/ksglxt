package com.xjl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@TableName("booking")
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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

    private String rejectReason;

    private Long approverId;

    private LocalDateTime approveTime;

    @TableLogic
    private String delFlag;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
