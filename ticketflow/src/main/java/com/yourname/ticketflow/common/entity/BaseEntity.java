package com.yourname.ticketflow.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类 - 公共字段
 */
@Data
public abstract class BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 租户ID - 多租户数据隔离 */
    private Long tenantId;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
