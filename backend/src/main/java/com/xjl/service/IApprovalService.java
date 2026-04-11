package com.xjl.service;

import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.BookingVO;

public interface IApprovalService {

    PageResult<BookingVO> pendingList(PageQuery query);

    PageResult<BookingVO> allList(PageQuery query, String status, Long classroomId, String startDate, String endDate);

    void approve(Long userId, Long id);

    void reject(Long userId, Long id, String reason);
}
