package com.yourname.ticketflow.modules.permission.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.permission.entity.SysPermission;
import com.yourname.ticketflow.modules.permission.entity.SysRolePermission;
import com.yourname.ticketflow.modules.permission.mapper.SysPermissionMapper;
import com.yourname.ticketflow.modules.permission.mapper.SysRolePermissionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    @Operation(summary = "权限树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<?> tree() {
        List<SysPermission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSort));
        return Result.ok(all);
    }

    @Operation(summary = "创建权限")
    @PostMapping
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<?> create(@RequestBody SysPermission permission) {
        permissionMapper.insert(permission);
        return Result.ok(permission);
    }

    @Operation(summary = "为角色分配权限")
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<Void> assignPermissions(@RequestParam Long roleId,
                                           @RequestBody List<Long> permissionIds) {
        // 删除旧的权限关联
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        // 添加新的权限关联
        for (Long permId : permissionIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
        return Result.ok();
    }

    @Operation(summary = "获取角色的权限ID列表")
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<?> rolePermissions(@PathVariable Long roleId) {
        List<SysRolePermission> list = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        return Result.ok(list.stream().map(SysRolePermission::getPermissionId).toList());
    }
}
