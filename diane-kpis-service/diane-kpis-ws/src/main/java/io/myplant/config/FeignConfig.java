package io.myplant.config;

import io.myplant.feign.security.MyPlantBlockingFeignAuthConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableFeignClients({"io.myplant.seshat.api", "io.myplant.service"})
@Import(MyPlantBlockingFeignAuthConfiguration.class)
public class FeignConfig {
}