package com.yourname.ticketflow.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yourname.ticketflow.modules.system.entity.SysDictData;
import com.yourname.ticketflow.modules.system.entity.SysDictType;
import com.yourname.ticketflow.modules.system.mapper.SysDictDataMapper;
import com.yourname.ticketflow.modules.system.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public List<SysDictType> listTypes() {
        return dictTypeMapper.selectList(
                new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getStatus, 1));
    }

    public List<SysDictData> listDataByType(String dictType) {
        SysDictType type = dictTypeMapper.selectOne(
                new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, dictType));
        if (type == null) {
            return List.of();
        }
        return dictDataMapper.selectList(
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictTypeId, type.getId())
                        .eq(SysDictData::getStatus, 1)
                        .orderByAsc(SysDictData::getSort));
    }
}
