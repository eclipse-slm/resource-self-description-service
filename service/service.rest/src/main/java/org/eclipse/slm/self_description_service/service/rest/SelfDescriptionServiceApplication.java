package org.eclipse.slm.self_description_service.service.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "org.eclipse.slm.self_description_service")
@EnableAsync
@EnableConfigurationProperties
public class SelfDescriptionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SelfDescriptionServiceApplication.class, args);
    }

}
