package com.xjl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xjl.domain.R;
import com.xjl.domain.dto.request.LoginRequest;
import com.xjl.domain.dto.request.RegisterRequest;
import com.xjl.domain.dto.response.UserInfoVO;
import com.xjl.domain.entity.SysRole;
import com.xjl.domain.entity.SysUser;
import com.xjl.domain.entity.SysUserRole;
import com.xjl.mapper.SysRoleMapper;
import com.xjl.mapper.SysUserMapper;
import com.xjl.mapper.SysUserRoleMapper;
import com.xjl.security.JwtTokenUtil;
import com.xjl.service.IAuthService;
import com.xjl.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final ISysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate redisTemplate;

    private static final int MAX_LOGIN_FAIL = 5;
    private static final long LOCK_MINUTES = 30;

    @Override
    @Transactional
    public R<Void> register(RegisterRequest request) {
        // 校验用户名唯一
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, request.getUserName())
        );
        if (count > 0) {
            return R.fail("用户名已存在");
        }

        // 插入用户
        SysUser user = new SysUser();
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickName(request.getNickName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus("0");
        user.setDelFlag("0");
        user.setLoginFailCount(0);
        sysUserMapper.insert(user);

        // 分配默认角色
        String roleCode = request.getRoleCode();
        SysRole role = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode)
        );
        if (role == null) {
            return R.fail("角色不存在: " + roleCode);
        }
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        sysUserRoleMapper.insert(userRole);

        return R.ok();
    }

    @Override
    public R<Map<String, Object>> login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, request.getUserName())
        );
        if (user == null) {
            return R.fail("用户名或密码错误");
        }

        // 检查锁定状态（Redis）
        String lockKey = "auth:lock:" + user.getUserName();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            return R.fail("账号已锁定，请" + (ttl != null ? ttl / 60 : LOCK_MINUTES) + "分钟后重试");
        }

        // 检查账号状态
        if ("1".equals(user.getStatus())) {
            return R.fail("账号已停用");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 失败递增计数
            int failCount = (user.getLoginFailCount() != null ? user.getLoginFailCount() : 0) + 1;
            user.setLoginFailCount(failCount);

            if (failCount >= MAX_LOGIN_FAIL) {
                // 锁定账号
                user.setLockTime(LocalDateTime.now());
                redisTemplate.opsForValue().set(lockKey, String.valueOf(failCount), LOCK_MINUTES, TimeUnit.MINUTES);
                user.setLoginFailCount(0);
            }
            sysUserMapper.updateById(user);
            return R.fail("用户名或密码错误，剩余尝试次数: " + Math.max(0, MAX_LOGIN_FAIL - failCount));
        }

        // 登录成功，清零失败计数
        user.setLoginFailCount(0);
        user.setLockTime(null);
        sysUserMapper.updateById(user);

        // 生成JWT
        String token = jwtTokenUtil.generateToken(user.getUserName());

        // 获取用户信息
        UserInfoVO userInfo = sysUserService.getUserInfo(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);
        return R.ok(data);
    }

    @Override
    public R<Void> logout() {
        // JWT 无状态，登出由前端清除 token 即可
        // 如需黑名单可在此扩展
        return R.ok();
    }
}
