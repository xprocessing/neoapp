package com.neoapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neoapp.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
