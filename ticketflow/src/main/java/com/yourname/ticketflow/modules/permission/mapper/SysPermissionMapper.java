package com.yourname.ticketflow.modules.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yourname.ticketflow.modules.permission.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限 Mapper
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
}
