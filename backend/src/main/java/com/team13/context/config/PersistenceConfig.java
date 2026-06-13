package com.team13.context.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

/**
 * 持久层扫描单独成配置，便于 Web 层切片测试在无 SqlSessionFactory 时不注册 Mapper。
 */
@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@MapperScan("com.team13.context.mapper")
public class PersistenceConfig {
}
