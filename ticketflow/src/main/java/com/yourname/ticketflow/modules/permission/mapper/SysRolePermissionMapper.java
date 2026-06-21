package com.yourname.ticketflow.modules.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yourname.ticketflow.modules.permission.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联 Mapper
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}
