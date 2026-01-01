package com.example.web3wallet.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("SIWE (Sign-In with Ethereum) API")
                    .description("Spring Boot 기반의 SIWE 인증 API 문서입니다.")
                    .version("1.0.0")
            )
    }
}
