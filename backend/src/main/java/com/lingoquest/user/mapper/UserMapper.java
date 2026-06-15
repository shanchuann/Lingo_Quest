package com.lingoquest.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
