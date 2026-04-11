package com.xjl.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xjl.domain.entity.SysRole;
import com.xjl.domain.entity.SysUser;
import com.xjl.domain.entity.SysRoleMenu;
import com.xjl.domain.entity.SysUserRole;
import com.xjl.mapper.SysRoleMapper;
import com.xjl.mapper.SysRoleMenuMapper;
import com.xjl.mapper.SysUserMapper;
import com.xjl.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username)
        );
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if ("1".equals(user.getStatus())) {
            throw new DisabledException("账号已停用: " + username);
        }
        if ("1".equals(user.getDelFlag())) {
            throw new LockedException("账号已锁定: " + username);
        }

        Set<String> permissions = buildPermissions(user.getId());
        return new LoginUser(user, permissions);
    }

    private Set<String> buildPermissions(Long userId) {
        Set<String> permissions = new HashSet<>();

        // 查询用户角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return permissions;
        }

        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        // 查询角色信息
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getStatus, "0")
        );

        // 添加 ROLE_xxx 权限（用于 hasRole 检查）
        for (SysRole role : roles) {
            permissions.add("ROLE_" + role.getRoleCode());
        }

        // 查询角色关联的菜单权限
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)
        );

        if (!roleMenus.isEmpty()) {
            // 管理员拥有所有权限
            boolean isAdmin = roles.stream()
                    .anyMatch(r -> "ADMIN".equals(r.getRoleCode()));
            if (isAdmin) {
                permissions.add("*:*:*");
            }
        }

        return permissions;
    }
}
