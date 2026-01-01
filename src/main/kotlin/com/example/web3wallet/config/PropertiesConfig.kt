package com.example.web3wallet.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SiweProperties::class)
class PropertiesConfig
