package com.hmall.pay;

import com.hmall.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@MapperScan("com.hmall.pay.mapper")
//@ComponentScan(basePackages = {"com.hmall.api"})
@EnableFeignClients(basePackages  = "com.hmall.api.client",defaultConfiguration = DefaultFeignConfig.class)
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }
}

