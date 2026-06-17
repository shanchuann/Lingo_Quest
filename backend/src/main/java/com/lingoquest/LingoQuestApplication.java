package com.lingoquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class LingoQuestApplication {
    public static void main(String[] args) {
        SpringApplication.run(LingoQuestApplication.class, args);
    }
}
