package com.example.services.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking API")
                        .version("1.0.0")
                        .license(new License())
                        .description("API for Banking System with User & Wallet Management")
                        .contact(new Contact()
                                .name("API Support")
                                .email("nghiameow125@gmail.com")
                                .url("https://portfoliojimmynguyen.vercel.app/")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                );
    }

}
