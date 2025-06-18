package com.g47.cem.cemgateway.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class GatewaySwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server()
                        .url("http://localhost:" + serverPort + contextPath)
                        .description("Development Gateway server"),
                    new Server()
                        .url("https://your-production-domain.com" + contextPath)
                        .description("Production Gateway server")
                ));
    }

    @Bean
    public GroupedOpenApi gatewayApi() {
        return GroupedOpenApi.builder()
                .group("gateway")
                .displayName("Gateway Management APIs")
                .pathsToMatch("/gateway/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth-service")
                .displayName("Authentication Service APIs")
                .pathsToMatch("/auth/**")
                .build();
    }

    private Info apiInfo() {
        return new Info()
                .title("CEM Gateway Service API")
                .description("API Gateway for CEM microservices architecture")
                .version("1.0.0")
                .contact(new Contact()
                    .name("CEM Development Team")
                    .email("dev@cem.com")
                    .url("https://cem.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }
} 