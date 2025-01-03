package xyz.a5s7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
public class OrderBookApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderBookApplication.class, args);
    }
}
