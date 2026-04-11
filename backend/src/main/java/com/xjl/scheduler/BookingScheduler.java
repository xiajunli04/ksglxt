package com.xjl.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xjl.domain.entity.Booking;
import com.xjl.domain.entity.Notification;
import com.xjl.mapper.BookingMapper;
import com.xjl.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingMapper bookingMapper;
    private final NotificationMapper notificationMapper;

    /**
     * 每小时执行：自动完成已过期的预约
     * status=1(已通过) 且 end_time < now 的 booking -> status=4(完成)
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void autoComplete() {
        log.info("开始执行自动完成任务...");
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // 查询今天之前已通过的预约
        List<Booking> pastBookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getStatus, "1")
                        .lt(Booking::getBookingDate, today)
        );

        // 查询今天已过时段的预约
        List<Booking> todayExpired = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getStatus, "1")
                        .eq(Booking::getBookingDate, today)
                        .lt(Booking::getEndTime, currentTime)
        );

        pastBookings.addAll(todayExpired);

        for (Booking booking : pastBookings) {
            Booking update = new Booking();
            update.setId(booking.getId());
            update.setStatus("4");
            bookingMapper.updateById(update);

            Notification notification = new Notification();
            notification.setUserId(booking.getApplicantId());
            notification.setTitle("预约完成通知");
            notification.setContent("您的预约（日期: " + booking.getBookingDate() + "）已完成");
            notification.setType("system");
            notification.setIsRead("0");
            notificationMapper.insert(notification);
        }
        log.info("自动完成任务执行完毕，处理了{}条记录", pastBookings.size());
    }

    /**
     * 每天0:30执行：自动关闭超时未审批的申请
     * status=0(待审批) 且 create_time < 3天前 -> status=3(取消) + 创建通知
     */
    @Scheduled(cron = "0 30 0 * * ?")
    @Transactional
    public void autoClose() {
        log.info("开始执行自动关闭超时申请任务...");
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        List<Booking> expiredBookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getStatus, "0")
                        .lt(Booking::getCreateTime, threeDaysAgo)
        );

        for (Booking booking : expiredBookings) {
            Booking update = new Booking();
            update.setId(booking.getId());
            update.setStatus("3");
            bookingMapper.updateById(update);

            Notification notification = new Notification();
            notification.setUserId(booking.getApplicantId());
            notification.setTitle("预约超时取消通知");
            notification.setContent("您的预约申请（日期: " + booking.getBookingDate() + "）因超时未审批已自动取消");
            notification.setType("system");
            notification.setIsRead("0");
            notificationMapper.insert(notification);
        }
        log.info("自动关闭超时申请任务执行完毕，处理了{}条记录", expiredBookings.size());
    }

    /**
     * 每天8:00执行：提醒当天有预约的用户
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void remind() {
        log.info("开始执行预约提醒任务...");
        LocalDate today = LocalDate.now();

        List<Booking> todayBookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getBookingDate, today)
                        .in(Booking::getStatus, "0", "1")
        );

        for (Booking booking : todayBookings) {
            Notification notification = new Notification();
            notification.setUserId(booking.getApplicantId());
            notification.setTitle("预约提醒");
            notification.setContent("您今天有预约安排，请准时使用教室");
            notification.setType("reminder");
            notification.setIsRead("0");
            notificationMapper.insert(notification);
        }
        log.info("预约提醒任务执行完毕，处理了{}条记录", todayBookings.size());
    }
}
