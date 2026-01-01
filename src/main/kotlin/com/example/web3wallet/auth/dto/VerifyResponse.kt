package com.example.web3wallet.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "검증 성공 응답 DTO")
data class VerifyResponse(
    @Schema(description = "복구된 이더리움 주소", example = "0x1234...")
    val address: String,

    @Schema(description = "발급된 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String
)
