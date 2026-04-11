package com.xjl.domain.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardAnnouncementVO {
    private Long id;
    private String title;
    private String isTop;
    private LocalDateTime createTime;
}
