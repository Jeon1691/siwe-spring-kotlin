package com.example.web3wallet.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "SIWE 검증 요청 DTO")
data class SiweVerifyRequest(
    @Schema(description = "SIWE 전체 메시지 (EIP-4361 형식)", example = "example.com wants you to sign in with your Ethereum account: ...")
    @field:NotBlank val message: String,

    @Schema(description = "지갑 서명 (Hex String)", example = "0x...")
    @field:NotBlank val signature: String
)
