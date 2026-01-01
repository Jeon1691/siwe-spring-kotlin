package com.example.web3wallet.auth.dto

import jakarta.validation.constraints.NotBlank

data class SiweVerifyRequest(
    @field:NotBlank val message: String,
    @field:NotBlank val signature: String
)
