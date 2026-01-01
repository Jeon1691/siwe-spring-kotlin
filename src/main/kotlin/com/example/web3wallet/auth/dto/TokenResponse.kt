package com.example.web3wallet.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "OAuth2 토큰 응답 DTO")
data class TokenResponse(
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("access_token")
    val accessToken: String,

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("refresh_token")
    val refreshToken: String,

    @Schema(description = "토큰 타입", example = "Bearer")
    @JsonProperty("token_type")
    val tokenType: String = "Bearer",

    @Schema(description = "만료 시간 (초)", example = "3600")
    @JsonProperty("expires_in")
    val expiresIn: Long
)
