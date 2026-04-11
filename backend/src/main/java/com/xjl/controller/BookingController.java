package com.xjl.controller;

import com.xjl.domain.PageResult;
import com.xjl.domain.R;
import com.xjl.domain.dto.request.BookingRequest;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.BookingVO;
import com.xjl.security.LoginUser;
import com.xjl.service.IBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final IBookingService bookingService;

    @PostMapping
    public R<Void> create(@AuthenticationPrincipal LoginUser loginUser,
                          @Valid @RequestBody BookingRequest request) {
        bookingService.create(loginUser.getUserId(), request);
        return R.ok();
    }

    @GetMapping("/my")
    public R<PageResult<BookingVO>> myBookings(@AuthenticationPrincipal LoginUser loginUser,
                                               PageQuery query,
                                               @RequestParam(required = false) String status) {
        return R.ok(bookingService.myBookings(loginUser.getUserId(), query, status));
    }

    @GetMapping("/slot-info")
    public R<BookingVO> getSlotBooking(@RequestParam Long classroomId,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                       @RequestParam Long timeSlotId) {
        return R.ok(bookingService.getSlotBooking(classroomId, date, timeSlotId));
    }

    @GetMapping("/{id}")
    public R<BookingVO> getById(@AuthenticationPrincipal LoginUser loginUser,
                                @PathVariable Long id) {
        return R.ok(bookingService.getDetailById(loginUser.getUserId(), id));
    }

    @PutMapping("/{id}/cancel")
    public R<Void> cancel(@AuthenticationPrincipal LoginUser loginUser,
                          @PathVariable Long id) {
        bookingService.cancel(loginUser.getUserId(), id);
        return R.ok();
    }
}
