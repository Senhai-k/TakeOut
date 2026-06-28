package com.takeout;

import com.takeout.config.UploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(UploadProperties.class)
public class TakeoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(TakeoutApplication.class, args);
    }
}
