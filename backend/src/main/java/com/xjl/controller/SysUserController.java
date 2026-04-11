package com.xjl.controller;

import com.xjl.domain.PageResult;
import com.xjl.domain.R;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.request.PasswordUpdateRequest;
import com.xjl.domain.dto.request.ProfileUpdateRequest;
import com.xjl.domain.dto.response.UserInfoVO;
import com.xjl.domain.entity.SysUser;
import com.xjl.security.LoginUser;
import com.xjl.service.ISysUserService;
import com.xjl.util.FileUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;

    @GetMapping("/info")
    public R<UserInfoVO> getUserInfo(@AuthenticationPrincipal LoginUser loginUser) {
        return R.ok(userService.getUserInfo(loginUser.getUserId()));
    }

    @PutMapping("/info")
    public R<Void> updateProfile(@AuthenticationPrincipal LoginUser loginUser,
                                 @RequestBody ProfileUpdateRequest request) {
        userService.updateProfile(loginUser.getUserId(), request);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> updatePassword(@AuthenticationPrincipal LoginUser loginUser,
                                  @Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(loginUser.getUserId(), request);
        return R.ok();
    }

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/avatar")
    public R<String> uploadAvatar(@AuthenticationPrincipal LoginUser loginUser,
                                  @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }
        if (!FileUtil.isAllowed(file.getOriginalFilename())) {
            return R.fail("不支持的文件类型");
        }
        if (file.getSize() > FileUtil.MAX_SIZE) {
            return R.fail("文件大小不能超过5MB");
        }

        String newFilename = FileUtil.generateFileName(file.getOriginalFilename());
        try {
            java.nio.file.Path avatarPath = java.nio.file.Paths.get(uploadDir, "avatars");
            if (!java.nio.file.Files.exists(avatarPath)) {
                java.nio.file.Files.createDirectories(avatarPath);
            }
            file.transferTo(avatarPath.resolve(newFilename).toFile());
            String avatarUrl = "/uploads/avatars/" + newFilename;

            SysUser update = new SysUser();
            update.setId(loginUser.getUserId());
            update.setAvatar(avatarUrl);
            userService.updateById(update);
            return R.ok(avatarUrl);
        } catch (Exception e) {
            return R.fail("头像上传失败");
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<UserInfoVO>> getUserList(PageQuery query) {
        return R.ok(userService.getUserList(query));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        userService.updateStatus(id, status);
        return R.ok();
    }
}
