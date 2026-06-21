package com.yourname.ticketflow.modules.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yourname.ticketflow.modules.role.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
