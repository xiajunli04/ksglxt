package com.xjl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.BookingVO;
import com.xjl.domain.entity.Booking;
import com.xjl.domain.entity.Notification;
import com.xjl.mapper.BookingMapper;
import com.xjl.mapper.NotificationMapper;
import com.xjl.service.IApprovalService;
import com.xjl.service.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements IApprovalService {

    private final BookingMapper bookingMapper;
    private final NotificationMapper notificationMapper;
    private final IBookingService bookingService;

    @Override
    public PageResult<BookingVO> pendingList(PageQuery query) {
        Page<Booking> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStatus, "0")
                .orderByAsc(Booking::getCreateTime);
        Page<Booking> result = bookingMapper.selectPage(page, wrapper);
        return buildBookingVOPage(result);
    }

    @Override
    public PageResult<BookingVO> allList(PageQuery query, String status, Long classroomId, String startDate, String endDate) {
        Page<Booking> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Booking::getStatus, status);
        }
        if (classroomId != null) {
            wrapper.eq(Booking::getClassroomId, classroomId);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Booking::getBookingDate, LocalDate.parse(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Booking::getBookingDate, LocalDate.parse(endDate));
        }
        wrapper.orderByDesc(Booking::getCreateTime);
        Page<Booking> result = bookingMapper.selectPage(page, wrapper);
        return buildBookingVOPage(result);
    }

    @Override
    @Transactional
    public void approve(Long userId, Long id) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!"0".equals(booking.getStatus())) {
            throw new RuntimeException("仅待审批状态可审批");
        }

        Booking update = new Booking();
        update.setId(id);
        update.setStatus("1");
        update.setApproverId(userId);
        update.setApproveTime(LocalDateTime.now());
        bookingMapper.updateById(update);

        // 创建通知
        Notification notification = new Notification();
        notification.setUserId(booking.getApplicantId());
        notification.setTitle("预约审批通知");
        notification.setContent("您的预约申请已通过，预约日期: " + booking.getBookingDate());
        notification.setType("1");
        notification.setIsRead("0");
        notificationMapper.insert(notification);
    }

    @Override
    @Transactional
    public void reject(Long userId, Long id, String reason) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!"0".equals(booking.getStatus())) {
            throw new RuntimeException("仅待审批状态可驳回");
        }

        Booking update = new Booking();
        update.setId(id);
        update.setStatus("2");
        update.setApproverId(userId);
        update.setApproveTime(LocalDateTime.now());
        update.setRejectReason(reason);
        bookingMapper.updateById(update);

        // 创建通知
        Notification notification = new Notification();
        notification.setUserId(booking.getApplicantId());
        notification.setTitle("预约驳回通知");
        notification.setContent("您的预约申请已驳回，原因: " + reason);
        notification.setType("2");
        notification.setIsRead("0");
        notificationMapper.insert(notification);
    }

    private PageResult<BookingVO> buildBookingVOPage(Page<Booking> result) {
        List<BookingVO> voList = result.getRecords().stream()
                .map(b -> ((BookingServiceImpl) bookingService).toVOProxy(b))
                .collect(Collectors.toList());
        PageResult<BookingVO> pr = new PageResult<>();
        pr.setRecords(voList);
        pr.setTotal(result.getTotal());
        pr.setPageNum((int) result.getCurrent());
        pr.setPageSize((int) result.getSize());
        pr.setPages(result.getPages());
        return pr;
    }
}
