package com.team13.context;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.team13.context.mapper")
public class ContextPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContextPracticeApplication.class, args);
    }
}
