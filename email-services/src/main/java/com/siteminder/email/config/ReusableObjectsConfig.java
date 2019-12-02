package com.siteminder.email.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siteminder.email.client.config.MailClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ReusableObjectsConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public MailClientConfig mailClientConfig(){
        return new MailClientConfig();
    }
}
