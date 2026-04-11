package com.xjl.controller;

import com.xjl.domain.PageResult;
import com.xjl.domain.R;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.ClassroomVO;
import com.xjl.domain.entity.Classroom;
import com.xjl.domain.entity.TimeSlot;
import com.xjl.service.IClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final IClassroomService classroomService;

    @GetMapping("/list")
    public R<PageResult<ClassroomVO>> list(PageQuery query,
                                           @RequestParam(required = false) String building,
                                           @RequestParam(required = false) String roomType,
                                           @RequestParam(required = false) String status) {
        return R.ok(classroomService.list(query, building, roomType, status));
    }

    @GetMapping("/{id}")
    public R<ClassroomVO> getById(@PathVariable Long id) {
        return R.ok(classroomService.getDetailById(id));
    }

    @GetMapping("/{id}/slots")
    public R<List<TimeSlot>> getAvailableSlots(@PathVariable Long id,
                                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return R.ok(classroomService.getAvailableSlots(id, date));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> add(@RequestBody Classroom classroom) {
        classroomService.addClassroom(classroom);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> update(@PathVariable Long id, @RequestBody Classroom classroom) {
        classroom.setId(id);
        classroomService.updateClassroom(classroom);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> delete(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return R.ok();
    }
}
