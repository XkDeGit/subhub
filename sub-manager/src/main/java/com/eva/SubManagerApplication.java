package com.eva;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Sub Manager 服务启动类
 *
 * @author Your Name
 */
@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient  // 启用服务注册发现
public class SubManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubManagerApplication.class, args);
    }
}

