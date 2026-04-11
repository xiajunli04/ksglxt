package com.xjl.domain.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementVO {

    private Long id;

    private String title;

    private String content;

    private Long publisherId;

    private String publisherName;

    private String isTop;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
