package com.xjl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.ClassroomVO;
import com.xjl.domain.entity.Booking;
import com.xjl.domain.entity.Classroom;
import com.xjl.domain.entity.TimeSlot;
import com.xjl.mapper.BookingMapper;
import com.xjl.mapper.ClassroomMapper;
import com.xjl.mapper.TimeSlotMapper;
import com.xjl.service.IClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl extends ServiceImpl<ClassroomMapper, Classroom> implements IClassroomService {

    private final TimeSlotMapper timeSlotMapper;
    private final BookingMapper bookingMapper;

    @Override
    @Cacheable(value = "classroom:list")
    public PageResult<ClassroomVO> list(PageQuery query, String building, String roomType, String status) {
        Page<Classroom> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Classroom::getRoomName, query.getKeyword())
                    .or().like(Classroom::getRoomCode, query.getKeyword());
        }
        if (StringUtils.hasText(building)) {
            wrapper.eq(Classroom::getBuilding, building);
        }
        if (StringUtils.hasText(roomType)) {
            wrapper.eq(Classroom::getRoomType, roomType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Classroom::getStatus, status);
        }
        wrapper.orderByDesc(Classroom::getCreateTime);
        Page<Classroom> result = page(page, wrapper);

        List<ClassroomVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        PageResult<ClassroomVO> pr = new PageResult<>();
        pr.setRecords(voList);
        pr.setTotal(result.getTotal());
        pr.setPageNum((int) result.getCurrent());
        pr.setPageSize((int) result.getSize());
        pr.setPages(result.getPages());
        return pr;
    }

    @Override
    @Cacheable(value = "classroom:detail", key = "#id")
    public ClassroomVO getDetailById(Long id) {
        Classroom classroom = getById(id);
        if (classroom == null) {
            return null;
        }
        return toVO(classroom);
    }

    @Override
    @CacheEvict(value = "classroom:list", allEntries = true)
    public void addClassroom(Classroom classroom) {
        classroom.setDelFlag("0");
        save(classroom);
    }

    @Override
    @CacheEvict(value = {"classroom:list", "classroom:detail"}, allEntries = true)
    public void updateClassroom(Classroom classroom) {
        updateById(classroom);
    }

    @Override
    @CacheEvict(value = {"classroom:list", "classroom:detail"}, allEntries = true)
    public void deleteClassroom(Long id) {
        // 检查有无进行中预约
        long count = bookingMapper.selectCount(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getClassroomId, id)
                        .in(Booking::getStatus, "0", "1")
        );
        if (count > 0) {
            throw new RuntimeException("该教室存在进行中的预约，无法删除");
        }
        removeById(id);
    }

    @Override
    public List<TimeSlot> getAvailableSlots(Long classroomId, LocalDate date) {
        // 查询所有时间段
        List<TimeSlot> allSlots = timeSlotMapper.selectList(
                new LambdaQueryWrapper<TimeSlot>().orderByAsc(TimeSlot::getSortOrder)
        );
        // 查询已被预约的时段（状态0=待审批, 1=已通过 视为已占用）
        List<Booking> booked = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getClassroomId, classroomId)
                        .eq(Booking::getBookingDate, date)
                        .in(Booking::getStatus, "0", "1")
        );
        List<Long> bookedSlotIds = booked.stream()
                .map(Booking::getTimeSlotId)
                .collect(Collectors.toList());
        // 标记每个时段是否可预约
        allSlots.forEach(slot -> slot.setAvailable(!bookedSlotIds.contains(slot.getId())));
        return allSlots;
    }

    private ClassroomVO toVO(Classroom c) {
        ClassroomVO vo = new ClassroomVO();
        vo.setId(c.getId());
        vo.setRoomCode(c.getRoomCode());
        vo.setRoomName(c.getRoomName());
        vo.setBuilding(c.getBuilding());
        vo.setFloor(c.getFloor());
        vo.setCapacity(c.getCapacity());
        vo.setRoomType(c.getRoomType());
        vo.setFacilities(c.getFacilities());
        vo.setStatus(c.getStatus());
        vo.setImage(c.getImage());
        vo.setRemark(c.getRemark());
        vo.setCreateTime(c.getCreateTime());
        vo.setAvailable("0".equals(c.getStatus()));
        return vo;
    }
}
