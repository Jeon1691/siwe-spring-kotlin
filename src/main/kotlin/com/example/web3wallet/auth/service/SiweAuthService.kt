package com.example.web3wallet.auth.service

import com.example.web3wallet.auth.dto.NonceResponse
import com.example.web3wallet.auth.dto.SiweVerifyRequest
import com.example.web3wallet.auth.dto.VerifyResponse
import com.example.web3wallet.auth.nonce.NonceService
import com.example.web3wallet.auth.siwe.SiweParser
import com.example.web3wallet.auth.siwe.SiweVerifier
import com.example.web3wallet.config.SiweProperties
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

@Service
class SiweAuthService(
    private val nonceService: NonceService,
    private val props: SiweProperties
) {
    private val rng = SecureRandom()

    fun issueNonce(): NonceResponse =
        NonceResponse(nonceService.issueNonce())

    fun verify(req: SiweVerifyRequest): VerifyResponse {
        val siwe = SiweParser.parse(req.message)

        // Basic field checks
        if (props.allowedDomains.isNotEmpty() && siwe.domain !in props.allowedDomains) {
            throw IllegalArgumentException("Domain not allowed")
        }
        if (props.allowedChainIds.isNotEmpty() && siwe.chainId !in props.allowedChainIds) {
            throw IllegalArgumentException("Chain ID not allowed")
        }

        // Time window checks (with small skew)
        val now = Instant.now()
        val skew = props.clockSkewSeconds
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

        // Minimal access token (replace with JWT/session in real deployment)
        val tokenBytes = ByteArray(32).also { rng.nextBytes(it) }
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)

        return VerifyResponse(address = recovered, accessToken = token)
    }
}
