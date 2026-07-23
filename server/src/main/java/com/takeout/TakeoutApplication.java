package com.takeout;

import com.takeout.config.UploadProperties;
import com.takeout.config.RateLimitProperties;
import com.takeout.config.AuthSecurityProperties;
import com.takeout.config.WechatProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({UploadProperties.class, RateLimitProperties.class, AuthSecurityProperties.class, WechatProperties.class})
@EnableScheduling
public class TakeoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(TakeoutApplication.class, args);
    }
}
