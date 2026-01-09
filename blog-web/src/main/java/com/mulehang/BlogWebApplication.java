package com.mulehang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mulehang.blog.mapper")
public class BlogWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogWebApplication.class, args);
    }

}
