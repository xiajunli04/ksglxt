package com.xjl.domain.dto.response;

import lombok.Data;

@Data
public class DashboardAvailableClassroomVO {
    private Long id;
    private String roomCode;
    private String roomName;
    private Integer capacity;
    private String roomType;
    private String availableSlots;
}
