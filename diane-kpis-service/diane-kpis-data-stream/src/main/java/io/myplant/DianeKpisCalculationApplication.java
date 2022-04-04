package io.myplant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask
@SpringBootApplication
@EnableConfigurationProperties(DianeKpisProperties.class)
public class DianeKpisCalculationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DianeKpisCalculationApplication.class, args);
    }
}
