package com.yourname.ticketflow.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yourname.ticketflow.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    private Long dictTypeId;
    private String dictLabel;
    private String dictValue;
    private String cssClass;
    private Integer sort;
    private Integer status;
    private String remark;
}
