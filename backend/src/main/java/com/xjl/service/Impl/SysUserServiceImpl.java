package com.xjl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.request.PasswordUpdateRequest;
import com.xjl.domain.dto.request.ProfileUpdateRequest;
import com.xjl.domain.dto.response.UserInfoVO;
import com.xjl.domain.entity.SysRole;
import com.xjl.domain.entity.SysUser;
import com.xjl.domain.entity.SysUserRole;
import com.xjl.mapper.SysRoleMapper;
import com.xjl.mapper.SysUserMapper;
import com.xjl.mapper.SysUserRoleMapper;
import com.xjl.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            return null;
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setNickName(user.getNickName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setSex(user.getSex());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());

        // 查询角色列表
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
            vo.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    public PageResult<UserInfoVO> getUserList(PageQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysUser::getUserName, query.getKeyword())
                    .or().like(SysUser::getNickName, query.getKeyword())
                    .or().like(SysUser::getPhone, query.getKeyword());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = page(page, wrapper);

        List<UserInfoVO> voList = result.getRecords().stream().map(user -> {
            UserInfoVO vo = new UserInfoVO();
            vo.setId(user.getId());
            vo.setUserName(user.getUserName());
            vo.setNickName(user.getNickName());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setSex(user.getSex());
            vo.setAvatar(user.getAvatar());
            vo.setStatus(user.getStatus());
            vo.setCreateTime(user.getCreateTime());

            List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
            );
            if (!userRoles.isEmpty()) {
                List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
                List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
                vo.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
            }
            return vo;
        }).collect(Collectors.toList());

        PageResult<UserInfoVO> pr = new PageResult<>();
        pr.setRecords(voList);
        pr.setTotal(result.getTotal());
        pr.setPageNum((int) result.getCurrent());
        pr.setPageSize((int) result.getSize());
        pr.setPages(result.getPages());
        return pr;
    }

    @Override
    public void updateStatus(Long id, String status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        updateById(user);
    }

    @Override
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setNickName(request.getNickName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setSex(request.getSex());
        updateById(user);
    }

    @Override
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(update);
    }
}
