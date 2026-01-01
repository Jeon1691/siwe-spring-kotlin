package com.example.web3wallet.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "siwe")
data class SiweProperties(
    val allowedDomains: List<String> = emptyList(),
    val allowedChainIds: List<Long> = emptyList(),
    val nonceTtlMinutes: Long = 10,
    val clockSkewSeconds: Long = 60
)
