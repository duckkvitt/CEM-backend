package com.g47.cem.cemgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class GatewaySwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CEM Gateway Service")
                        .description("API Gateway for CEM microservices. " +
                                    "Access individual services: " +
                                    "Auth (http://localhost:8081/api/auth/swagger-ui.html), " +
                                    "Customer (http://localhost:8082/api/customer/swagger-ui.html), " +
                                    "Device (http://localhost:8083/api/device/swagger-ui.html)")
                        .version("1.0.0"));
    }
}