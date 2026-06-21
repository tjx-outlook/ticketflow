package com.yourname.ticketflow.modules.role.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.role.entity.SysRole;
import com.yourname.ticketflow.modules.role.entity.SysUserRole;
import com.yourname.ticketflow.modules.role.mapper.SysRoleMapper;
import com.yourname.ticketflow.modules.role.mapper.SysUserRoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Operation(summary = "角色列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(roleMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSort)));
    }

    @Operation(summary = "所有角色")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<?> all() {
        return Result.ok(roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1)));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<?> create(@RequestBody SysRole role) {
        roleMapper.insert(role);
        return Result.ok(role);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<?> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleMapper.updateById(role);
        return Result.ok(role);
    }

    @Operation(summary = "为用户分配角色")
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<Void> assignRoles(@RequestParam Long userId, @RequestBody List<Long> roleIds) {
        // 删除旧的角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 添加新的角色关联
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
        return Result.ok();
    }

    @Operation(summary = "获取用户的角色")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<?> userRoles(@PathVariable Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        return Result.ok(userRoles.stream().map(SysUserRole::getRoleId).toList());
    }
}
