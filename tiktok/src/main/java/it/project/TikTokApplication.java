package it.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan(basePackages = {"it.project.config.property"})
public class TikTokApplication {
    static void main(String[] args) {
        SpringApplication.run(TikTokApplication.class, args);
    }
}
