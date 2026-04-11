package com.xjl.controller;

import com.xjl.domain.PageResult;
import com.xjl.domain.R;
import com.xjl.domain.dto.request.AnnouncementRequest;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.AnnouncementVO;
import com.xjl.security.LoginUser;
import com.xjl.service.IAnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final IAnnouncementService announcementService;

    @GetMapping("/list")
    public R<PageResult<AnnouncementVO>> list(PageQuery query) {
        return R.ok(announcementService.list(query));
    }

    @GetMapping("/{id}")
    public R<AnnouncementVO> getById(@PathVariable Long id) {
        return R.ok(announcementService.getDetailById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> add(@AuthenticationPrincipal LoginUser loginUser,
                       @Valid @RequestBody AnnouncementRequest request) {
        announcementService.add(loginUser.getUserId(), request);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> update(@PathVariable Long id,
                          @Valid @RequestBody AnnouncementRequest request) {
        announcementService.update(id, request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return R.ok();
    }
}
