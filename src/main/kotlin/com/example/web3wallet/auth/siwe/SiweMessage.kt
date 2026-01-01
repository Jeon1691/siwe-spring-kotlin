package com.example.web3wallet.auth.siwe

import java.time.Instant

data class SiweMessage(
    val domain: String,
    val address: String,
    val statement: String?,
    val uri: String,
    val version: String,
    val chainId: Long,
    val nonce: String,
    val issuedAt: Instant,
    val expirationTime: Instant? = null,
    val notBefore: Instant? = null,
    val requestId: String? = null,
    val resources: List<String> = emptyList()
)
