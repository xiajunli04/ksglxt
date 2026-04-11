package com.xjl.domain.dto.request;

import lombok.Data;

@Data
public class PageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String keyword;
}
