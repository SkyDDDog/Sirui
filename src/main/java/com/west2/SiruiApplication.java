package com.west2;

import com.west2.utils.RedisUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.west2")
public class SiruiApplication {


    public static void main(String[] args) {
        SpringApplication.run(SiruiApplication.class, args);
    }

}
