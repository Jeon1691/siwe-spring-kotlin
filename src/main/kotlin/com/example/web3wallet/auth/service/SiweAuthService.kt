package com.example.web3wallet.auth.service

import com.example.web3wallet.auth.dto.NonceResponse
import com.example.web3wallet.auth.dto.RefreshTokenRequest
import com.example.web3wallet.auth.dto.SiweVerifyRequest
import com.example.web3wallet.auth.dto.TokenResponse
import com.example.web3wallet.auth.jwt.TokenProvider
import com.example.web3wallet.auth.nonce.NonceService
import com.example.web3wallet.auth.repository.RefreshTokenRepository
import com.example.web3wallet.auth.siwe.SiweParser
import com.example.web3wallet.auth.siwe.SiweVerifier
import com.example.web3wallet.config.JwtProperties
import com.example.web3wallet.config.SiweProperties
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SiweAuthService(
    private val nonceService: NonceService,
    private val tokenProvider: TokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val siweProps: SiweProperties,
    private val jwtProps: JwtProperties
) {

    fun issueNonce(): NonceResponse =
        NonceResponse(nonceService.issueNonce())

    fun verify(req: SiweVerifyRequest): TokenResponse {
        val siwe = SiweParser.parse(req.message)

        // Basic field checks
        if (siweProps.allowedDomains.isNotEmpty()) {
            val isAllowed = siweProps.allowedDomains.any { allowed ->
                if (allowed.startsWith("*.")) {
                    val suffix = allowed.removePrefix("*.")
                    siwe.domain.endsWith(suffix) || siwe.domain == suffix
                } else {
                    siwe.domain == allowed
                }
            }
            if (!isAllowed) {
                throw IllegalArgumentException("Domain not allowed: ${siwe.domain}")
            }
        }

        if (siweProps.allowedChainIds.isNotEmpty() && siwe.chainId !in siweProps.allowedChainIds) {
            throw IllegalArgumentException("Chain ID not allowed")
        }

        // Time window checks (with small skew)
        val now = Instant.now()
        val skew = siweProps.clockSkewSeconds
        val latestIssuedAt = now.plusSeconds(skew)
        if (siwe.issuedAt.isAfter(latestIssuedAt)) {
            throw IllegalArgumentException("Issued At is in the future")
        }
        siwe.notBefore?.let {
            if (now.plusSeconds(skew).isBefore(it)) throw IllegalArgumentException("Not Before not reached")
        }
        siwe.expirationTime?.let {
            if (now.minusSeconds(skew).isAfter(it)) throw IllegalArgumentException("Message expired")
        }

        // One-time nonce
        if (!nonceService.consumeNonce(siwe.nonce)) {
            throw IllegalArgumentException("Invalid/expired nonce")
        }

        // Signature verification (recover address and compare)
        val recovered = SiweVerifier.recoverAddressFromPersonalSign(req.message, req.signature)
        if (recovered.lowercase() != siwe.address.lowercase()) {
            throw IllegalArgumentException("Signature does not match address")
        }

        // Issue Tokens
        return issueTokens(recovered)
    }

    fun refresh(req: RefreshTokenRequest): TokenResponse {
        val refreshToken = req.refreshToken

        // 1. Validate Refresh Token format & signature
        if (!tokenProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        // 2. Check if it exists in store (Rotation or Revocation check)
        val address = refreshTokenRepository.findByToken(refreshToken)
            ?: throw IllegalArgumentException("Refresh token not found or revoked")

        // 3. Rotate Refresh Token (Optional but recommended)
        // Delete old token
        refreshTokenRepository.delete(refreshToken)

        // Issue new tokens
        return issueTokens(address)
    }

    private fun issueTokens(address: String): TokenResponse {
        val accessToken = tokenProvider.createAccessToken(address)
        val refreshToken = tokenProvider.createRefreshToken(address)

        // Save Refresh Token
        refreshTokenRepository.save(refreshToken, address)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProps.accessTokenValiditySeconds
        )
    }
}
