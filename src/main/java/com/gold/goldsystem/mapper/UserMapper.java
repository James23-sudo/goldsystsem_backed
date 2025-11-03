package com.gold.goldsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gold.goldsystem.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<UserEntity>{
}
