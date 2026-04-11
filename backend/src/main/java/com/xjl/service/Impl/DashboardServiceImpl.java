package com.xjl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xjl.domain.dto.response.*;
import com.xjl.domain.entity.*;
import com.xjl.mapper.*;
import com.xjl.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final ClassroomMapper classroomMapper;
    private final BookingMapper bookingMapper;
    private final AnnouncementMapper announcementMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public DashboardStatsVO getStats(Long userId, String roleCode) {
        DashboardStatsVO vo = new DashboardStatsVO();

        if ("ADMIN".equals(roleCode)) {
            vo.setTotalClassrooms(classroomMapper.selectCount(
                    new LambdaQueryWrapper<Classroom>().eq(Classroom::getDelFlag, "0")
            ));
            vo.setPendingApprovals(bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>().eq(Booking::getStatus, "0")
            ));
            LocalDate today = LocalDate.now();
            vo.setTodayBookings(bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>()
                            .eq(Booking::getBookingDate, today)
                            .in(Booking::getStatus, "0", "1")
            ));
            LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = LocalDate.now().atTime(LocalTime.MAX);
            vo.setMonthBookings(bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>()
                            .ge(Booking::getCreateTime, monthStart)
                            .le(Booking::getCreateTime, monthEnd)
            ));
        } else if ("TEACHER".equals(roleCode)) {
            Long total = bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>().eq(Booking::getApplicantId, userId)
            );
            vo.setMyBookings(total);
            Long approved = bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>()
                            .eq(Booking::getApplicantId, userId)
                            .in(Booking::getStatus, "1", "4")
            );
            long rate = total > 0 ? (approved * 100 / total) : 0;
            vo.setApprovalRate(rate);
            LocalDate today = LocalDate.now();
            vo.setTodayBookings(bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>()
                            .eq(Booking::getApplicantId, userId)
                            .eq(Booking::getBookingDate, today)
                            .in(Booking::getStatus, "0", "1")
            ));
            LocalDate weekStart = today.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
            vo.setWeekBookings(bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>()
                            .eq(Booking::getApplicantId, userId)
                            .ge(Booking::getBookingDate, weekStart)
                            .le(Booking::getBookingDate, weekEnd)
            ));
        } else {
            // STUDENT
            vo.setMyBookings(bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>().eq(Booking::getApplicantId, userId)
            ));
            vo.setMyPending(bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>()
                            .eq(Booking::getApplicantId, userId)
                            .eq(Booking::getStatus, "0")
            ));
            LocalDate today = LocalDate.now();
            vo.setMyToday(bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>()
                            .eq(Booking::getApplicantId, userId)
                            .eq(Booking::getBookingDate, today)
                            .in(Booking::getStatus, "0", "1")
            ));
        }
        return vo;
    }

    @Override
    public List<DashboardAnnouncementVO> getRecentAnnouncements() {
        List<Announcement> list = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getStatus, "1")
                        .orderByDesc(Announcement::getIsTop)
                        .orderByDesc(Announcement::getCreateTime)
                        .last("LIMIT 5")
        );
        return list.stream().map(a -> {
            DashboardAnnouncementVO vo = new DashboardAnnouncementVO();
            vo.setId(a.getId());
            vo.setTitle(a.getTitle());
            vo.setIsTop(a.getIsTop());
            vo.setCreateTime(a.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DashboardHotClassroomVO> getHotClassrooms() {
        // Count bookings per classroom in the last 30 days, top 5
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<Booking> recentBookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .ge(Booking::getBookingDate, thirtyDaysAgo)
                        .in(Booking::getStatus, "1", "4")
        );

        Map<Long, Long> countMap = recentBookings.stream()
                .collect(Collectors.groupingBy(Booking::getClassroomId, Collectors.counting()));

        List<Map.Entry<Long, Long>> sorted = countMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<DashboardHotClassroomVO> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : sorted) {
            Classroom classroom = classroomMapper.selectById(entry.getKey());
            if (classroom != null) {
                DashboardHotClassroomVO vo = new DashboardHotClassroomVO();
                vo.setId(classroom.getId());
                vo.setRoomCode(classroom.getRoomCode());
                vo.setRoomName(classroom.getRoomName());
                vo.setCapacity(classroom.getCapacity());
                vo.setRoomType(classroom.getRoomType());
                vo.setBookingCount(entry.getValue());
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public List<DashboardAvailableClassroomVO> getAvailableClassrooms() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Get all available classrooms
        List<Classroom> classrooms = classroomMapper.selectList(
                new LambdaQueryWrapper<Classroom>()
                        .eq(Classroom::getStatus, "0")
                        .eq(Classroom::getDelFlag, "0")
        );

        // Get today's approved bookings
        List<Booking> todayBookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getBookingDate, today)
                        .in(Booking::getStatus, "0", "1")
        );

        Map<Long, List<Booking>> bookedMap = todayBookings.stream()
                .collect(Collectors.groupingBy(Booking::getClassroomId));

        List<DashboardAvailableClassroomVO> result = new ArrayList<>();
        for (Classroom room : classrooms) {
            List<Booking> roomBookings = bookedMap.getOrDefault(room.getId(), List.of());

            // Find free time slots (simplified: check if room has any free period)
            String availableSlots = computeAvailableSlots(roomBookings, now);

            if (availableSlots != null) {
                DashboardAvailableClassroomVO vo = new DashboardAvailableClassroomVO();
                vo.setId(room.getId());
                vo.setRoomCode(room.getRoomCode());
                vo.setRoomName(room.getRoomName());
                vo.setCapacity(room.getCapacity());
                vo.setRoomType(room.getRoomType());
                vo.setAvailableSlots(availableSlots);
                result.add(vo);
            }

            if (result.size() >= 5) break;
        }
        return result;
    }

    @Override
    public List<DashboardRecentBookingVO> getRecentBookings(Long userId, String roleCode) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        if (!"ADMIN".equals(roleCode)) {
            wrapper.eq(Booking::getApplicantId, userId);
        }
        wrapper.orderByDesc(Booking::getCreateTime).last("LIMIT 5");

        List<Booking> bookings = bookingMapper.selectList(wrapper);

        return bookings.stream().map(b -> {
            DashboardRecentBookingVO vo = new DashboardRecentBookingVO();
            vo.setId(b.getId());
            vo.setBookingDate(b.getBookingDate());
            vo.setStartTime(b.getStartTime());
            vo.setEndTime(b.getEndTime());
            vo.setStatus(b.getStatus());
            vo.setPurpose(b.getPurpose());

            Classroom classroom = classroomMapper.selectById(b.getClassroomId());
            if (classroom != null) {
                vo.setRoomName(classroom.getRoomName());
            }

            if ("ADMIN".equals(roleCode)) {
                SysUser applicant = sysUserMapper.selectById(b.getApplicantId());
                if (applicant != null) {
                    vo.setApplicantName(applicant.getNickName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private String computeAvailableSlots(List<Booking> bookings, LocalTime now) {
        // Define standard time slots
        List<TimeRange> slots = List.of(
                new TimeRange("第1-2节", LocalTime.of(8, 0), LocalTime.of(9, 40)),
                new TimeRange("第3-4节", LocalTime.of(10, 0), LocalTime.of(11, 40)),
                new TimeRange("第5-6节", LocalTime.of(14, 0), LocalTime.of(15, 40)),
                new TimeRange("第7-8节", LocalTime.of(16, 0), LocalTime.of(17, 40)),
                new TimeRange("晚上", LocalTime.of(19, 0), LocalTime.of(21, 0))
        );

        List<String> freeSlots = new ArrayList<>();
        for (TimeRange slot : slots) {
            // Skip past slots
            if (slot.end.isBefore(now)) continue;

            boolean occupied = bookings.stream().anyMatch(b ->
                    b.getStartTime().isBefore(slot.end) && b.getEndTime().isAfter(slot.start)
            );
            if (!occupied) {
                freeSlots.add(slot.name);
            }
        }

        if (freeSlots.isEmpty()) return null;
        return String.join("、", freeSlots) + "空";
    }

    private record TimeRange(String name, LocalTime start, LocalTime end) {}
}
