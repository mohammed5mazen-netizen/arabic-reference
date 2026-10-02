package com.mrsoft.arabicreference;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ArabicReferenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArabicReferenceApplication.class, args);
    }
}
