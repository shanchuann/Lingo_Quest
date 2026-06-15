package com.lingoquest.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.lingoquest.**.mapper")
public class MyBatisPlusConfig {
}
