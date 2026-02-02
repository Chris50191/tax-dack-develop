package com.searly.taxcontrol.sii.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Tax Dack SII API",
                version = "1.0.3",
                description = "基于 Spring Boot 2 的 SII 接口文档",
                contact = @Contact(name = "Searly", url = "https://www.searly.com"),
                license = @License(name = "Proprietary")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "本地环境")
        }
)
public class OpenApiConfig {
}
