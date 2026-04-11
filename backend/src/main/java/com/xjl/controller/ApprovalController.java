package com.xjl.controller;

import com.xjl.domain.PageResult;
import com.xjl.domain.R;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.BookingVO;
import com.xjl.security.LoginUser;
import com.xjl.service.IApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final IApprovalService approvalService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<BookingVO>> pendingList(PageQuery query) {
        return R.ok(approvalService.pendingList(query));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<BookingVO>> allList(PageQuery query,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) Long classroomId,
                                            @RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate) {
        return R.ok(approvalService.allList(query, status, classroomId, startDate, endDate));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> approve(@AuthenticationPrincipal LoginUser loginUser,
                           @PathVariable Long id) {
        approvalService.approve(loginUser.getUserId(), id);
        return R.ok();
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> reject(@AuthenticationPrincipal LoginUser loginUser,
                          @PathVariable Long id,
                          @RequestParam String reason) {
        approvalService.reject(loginUser.getUserId(), id, reason);
        return R.ok();
    }
}
