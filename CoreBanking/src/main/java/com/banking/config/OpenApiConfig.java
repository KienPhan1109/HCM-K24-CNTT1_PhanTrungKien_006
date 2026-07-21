package com.banking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CoreBanking System - Loan Application API")
                        .version("1.0.0")
                        .description("Hệ thống Ngân hàng Lõi CoreBanking - Module Quản lý & Phê duyệt Hồ sơ Vay Tín chấp (Credit Check Engine)")
                        .contact(new Contact()
                                .name("Dev Team CoreBanking")
                                .email("dev@corebanking.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
