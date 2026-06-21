package com.yourname.ticketflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.yourname.ticketflow.modules.**.mapper")
@EnableAsync
public class TicketflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketflowApplication.class, args);
    }
}
