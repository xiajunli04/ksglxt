package com.xjl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.BookingRequest;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.BookingVO;
import com.xjl.domain.entity.*;
import com.xjl.mapper.*;
import com.xjl.service.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements IBookingService {

    private final ClassroomMapper classroomMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final SysUserMapper sysUserMapper;

    private static final Map<String, String> STATUS_MAP = Map.of(
            "0", "待审批",
            "1", "已通过",
            "2", "已驳回",
            "3", "已取消",
            "4", "已完成"
    );

    @Override
    public void create(Long userId, BookingRequest request) {
        // 校验课室状态
        Classroom classroom = classroomMapper.selectById(request.getClassroomId());
        if (classroom == null) {
            throw new RuntimeException("教室不存在");
        }
        if (!"0".equals(classroom.getStatus())) {
            throw new RuntimeException("该教室当前不可预约");
        }

        // 冲突检测
        LocalDate bookingDate = LocalDate.parse(request.getBookingDate());
        long count = count(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getClassroomId, request.getClassroomId())
                        .eq(Booking::getBookingDate, bookingDate)
                        .eq(Booking::getTimeSlotId, request.getTimeSlotId())
                        .in(Booking::getStatus, "0", "1")
        );
        if (count > 0) {
            throw new RuntimeException("该教室在所选日期和时段已被预约");
        }

        // 获取时间段信息
        TimeSlot timeSlot = timeSlotMapper.selectById(request.getTimeSlotId());

        Booking booking = new Booking();
        booking.setApplicantId(userId);
        booking.setClassroomId(request.getClassroomId());
        booking.setBookingDate(bookingDate);
        booking.setTimeSlotId(request.getTimeSlotId());
        booking.setStartTime(timeSlot != null ? timeSlot.getStartTime() : null);
        booking.setEndTime(timeSlot != null ? timeSlot.getEndTime() : null);
        booking.setPurpose(request.getPurpose());
        booking.setExpectedAttendance(request.getExpectedAttendance());
        booking.setContactPhone(request.getContactPhone());
        booking.setAttachmentUrl(request.getAttachmentUrl());
        booking.setStatus("0");
        booking.setDelFlag("0");
        save(booking);
    }

    @Override
    public PageResult<BookingVO> myBookings(Long userId, PageQuery query, String status) {
        Page<Booking> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getApplicantId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Booking::getStatus, status);
        }
        wrapper.orderByDesc(Booking::getCreateTime);
        Page<Booking> result = page(page, wrapper);
        List<BookingVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        PageResult<BookingVO> pr = new PageResult<>();
        pr.setRecords(voList);
        pr.setTotal(result.getTotal());
        pr.setPageNum((int) result.getCurrent());
        pr.setPageSize((int) result.getSize());
        pr.setPages(result.getPages());
        return pr;
    }

    @Override
    public BookingVO getDetailById(Long userId, Long id) {
        Booking booking = getById(id);
        if (booking == null) {
            return null;
        }
        // 权限校验：管理员可看所有，普通用户只能看自己的
        // 在 Controller 层通过角色判断，此处简化处理
        return toVO(booking);
    }

    @Override
    public void cancel(Long userId, Long id) {
        Booking booking = getById(id);
        if (booking == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!booking.getApplicantId().equals(userId)) {
            throw new RuntimeException("只能取消自己的预约");
        }
        if (!"0".equals(booking.getStatus()) && !"1".equals(booking.getStatus())) {
            throw new RuntimeException("仅待审批或已通过状态可取消");
        }
        Booking update = new Booking();
        update.setId(id);
        update.setStatus("3");
        updateById(update);
    }

    @Override
    public BookingVO getSlotBooking(Long classroomId, LocalDate date, Long timeSlotId) {
        Booking booking = getOne(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getClassroomId, classroomId)
                        .eq(Booking::getBookingDate, date)
                        .eq(Booking::getTimeSlotId, timeSlotId)
                        .in(Booking::getStatus, "0", "1")
                        .last("LIMIT 1")
        );
        if (booking == null) {
            return null;
        }
        return toVO(booking);
    }

    public BookingVO toVOProxy(Booking b) {
        return toVO(b);
    }

    private BookingVO toVO(Booking b) {
        BookingVO vo = new BookingVO();
        vo.setId(b.getId());
        vo.setApplicantId(b.getApplicantId());
        vo.setClassroomId(b.getClassroomId());
        vo.setBookingDate(b.getBookingDate());
        vo.setTimeSlotId(b.getTimeSlotId());
        vo.setStartTime(b.getStartTime());
        vo.setEndTime(b.getEndTime());
        vo.setPurpose(b.getPurpose());
        vo.setExpectedAttendance(b.getExpectedAttendance());
        vo.setContactPhone(b.getContactPhone());
        vo.setAttachmentUrl(b.getAttachmentUrl());
        vo.setStatus(b.getStatus());
        vo.setStatusText(STATUS_MAP.getOrDefault(b.getStatus(), "未知"));
        vo.setRejectReason(b.getRejectReason());
        vo.setApproverId(b.getApproverId());
        vo.setApproveTime(b.getApproveTime());
        vo.setCreateTime(b.getCreateTime());

        // 关联名称
        SysUser applicant = sysUserMapper.selectById(b.getApplicantId());
        if (applicant != null) {
            vo.setApplicantName(applicant.getNickName() != null ? applicant.getNickName() : applicant.getUserName());
        }
        Classroom classroom = classroomMapper.selectById(b.getClassroomId());
        if (classroom != null) {
            vo.setClassroomName(classroom.getRoomName());
        }
        TimeSlot timeSlot = timeSlotMapper.selectById(b.getTimeSlotId());
        if (timeSlot != null) {
            vo.setTimeSlotName(timeSlot.getSlotName());
        }
        return vo;
    }
}
