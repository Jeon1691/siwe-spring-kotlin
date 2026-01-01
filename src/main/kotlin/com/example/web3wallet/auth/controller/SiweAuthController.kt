package com.example.web3wallet.auth.controller

import com.example.web3wallet.auth.dto.NonceResponse
import com.example.web3wallet.auth.dto.SiweVerifyRequest
import com.example.web3wallet.auth.dto.VerifyResponse
import com.example.web3wallet.auth.service.SiweAuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth/siwe")
class SiweAuthController(
    private val siweAuthService: SiweAuthService
) {

    @GetMapping("/nonce")
    fun nonce(): NonceResponse = siweAuthService.issueNonce()

    @PostMapping("/verify")
    fun verify(@RequestBody @Valid req: SiweVerifyRequest): VerifyResponse =
        siweAuthService.verify(req)
}
