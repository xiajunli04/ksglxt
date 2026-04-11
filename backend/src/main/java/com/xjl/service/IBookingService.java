package com.xjl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.BookingRequest;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.BookingVO;
import com.xjl.domain.entity.Booking;

import java.time.LocalDate;

public interface IBookingService extends IService<Booking> {

    void create(Long userId, BookingRequest request);

    PageResult<BookingVO> myBookings(Long userId, PageQuery query, String status);

    BookingVO getDetailById(Long userId, Long id);

    void cancel(Long userId, Long id);

    BookingVO getSlotBooking(Long classroomId, LocalDate date, Long timeSlotId);
}
