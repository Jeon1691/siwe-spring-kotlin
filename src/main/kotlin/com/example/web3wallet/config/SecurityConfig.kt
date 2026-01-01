package com.example.web3wallet.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.GET, "/api/auth/siwe/nonce").permitAll()
                it.requestMatchers(HttpMethod.POST, "/api/auth/siwe/verify").permitAll()
                it.anyRequest().authenticated()
            }

        return http.build()
    }
}
