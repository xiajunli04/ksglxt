package com.xjl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.ClassroomVO;
import com.xjl.domain.entity.Classroom;
import com.xjl.domain.entity.TimeSlot;

import java.time.LocalDate;
import java.util.List;

public interface IClassroomService extends IService<Classroom> {

    PageResult<ClassroomVO> list(PageQuery query, String building, String roomType, String status);

    ClassroomVO getDetailById(Long id);

    void addClassroom(Classroom classroom);

    void updateClassroom(Classroom classroom);

    void deleteClassroom(Long id);

    List<TimeSlot> getAvailableSlots(Long classroomId, LocalDate date);
}
