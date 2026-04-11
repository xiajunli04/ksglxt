package com.xjl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.request.PasswordUpdateRequest;
import com.xjl.domain.dto.request.ProfileUpdateRequest;
import com.xjl.domain.dto.response.UserInfoVO;
import com.xjl.domain.entity.SysUser;

public interface ISysUserService extends IService<SysUser> {

    UserInfoVO getUserInfo(Long userId);

    PageResult<UserInfoVO> getUserList(PageQuery query);

    void updateStatus(Long id, String status);

    void updateProfile(Long userId, ProfileUpdateRequest request);

    void updatePassword(Long userId, PasswordUpdateRequest request);
}
