package com.lingoquest.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoquest.learning.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
