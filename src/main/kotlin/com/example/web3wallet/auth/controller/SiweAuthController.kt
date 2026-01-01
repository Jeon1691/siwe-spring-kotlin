package com.example.web3wallet.auth.controller

import com.example.web3wallet.auth.dto.NonceResponse
import com.example.web3wallet.auth.dto.RefreshTokenRequest
import com.example.web3wallet.auth.dto.SiweVerifyRequest
import com.example.web3wallet.auth.dto.TokenResponse
import com.example.web3wallet.auth.service.SiweAuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "SIWE Auth", description = "Sign-In with Ethereum 인증 API")
@RestController
@RequestMapping("/api/auth/siwe")
class SiweAuthController(
    private val siweAuthService: SiweAuthService
) {

    @Operation(summary = "Nonce 발급", description = "서명을 위한 랜덤 Nonce를 발급합니다.")
    @GetMapping("/nonce")
    fun nonce(): NonceResponse = siweAuthService.issueNonce()

    @Operation(summary = "서명 검증 및 로그인", description = "클라이언트가 서명한 SIWE 메시지를 검증하고 액세스 토큰(JWT)을 발급합니다.")
    @PostMapping("/verify")
    fun verify(@RequestBody @Valid req: SiweVerifyRequest): TokenResponse =
        siweAuthService.verify(req)

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    fun refresh(@RequestBody @Valid req: RefreshTokenRequest): TokenResponse =
        siweAuthService.refresh(req)
}
