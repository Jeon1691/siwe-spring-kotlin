package com.example.web3wallet.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Nonce 응답 DTO")
data class NonceResponse(
    @Schema(description = "발급된 랜덤 Nonce 값", example = "a1b2c3d4...")
    val nonce: String
)
