package com.lingoquest.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.game.entity.Game;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GameMapper extends BaseMapper<Game> {
}
