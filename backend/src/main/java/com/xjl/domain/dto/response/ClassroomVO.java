package com.xjl.domain.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassroomVO {

    private Long id;

    private String roomCode;

    private String roomName;

    private String building;

    private Integer floor;

    private Integer capacity;

    private String roomType;

    private String facilities;

    private String status;

    private String image;

    private String remark;

    private LocalDateTime createTime;

    /**
     * 当前可用状态（逻辑计算）
     */
    private Boolean available;
}
