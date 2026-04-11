package com.xjl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@TableName("time_slot")
public class TimeSlot implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String slotName;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer sortOrder;

    /** 非数据库字段：该时段是否可预约 */
    @TableField(exist = false)
    private Boolean available;
}
