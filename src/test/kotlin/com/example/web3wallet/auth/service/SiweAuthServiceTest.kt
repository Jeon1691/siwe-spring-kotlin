package com.example.web3wallet.auth.service

import com.example.web3wallet.auth.dto.SiweVerifyRequest
import com.example.web3wallet.auth.jwt.TokenProvider
import com.example.web3wallet.auth.nonce.NonceService
import com.example.web3wallet.auth.repository.RefreshTokenRepository
import com.example.web3wallet.config.JwtProperties
import com.example.web3wallet.config.SiweProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class SiweAuthServiceTest {

    @Mock
    private lateinit var nonceService: NonceService
    @Mock
    private lateinit var tokenProvider: TokenProvider
    @Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    private lateinit var siweAuthService: SiweAuthService
    private val siweProps = SiweProperties(
        allowedDomains = listOf("example.com"),
        allowedChainIds = listOf(1L),
        nonceTtlMinutes = 10,
        clockSkewSeconds = 60
    )
    private val jwtProps = JwtProperties(
        secret = "secret",
        accessTokenValiditySeconds = 3600,
        refreshTokenValiditySeconds = 86400
    )

    private val ecKeyPair = Keys.createEcKeyPair()
    private val address = "0x" + Keys.getAddress(ecKeyPair)

    @BeforeEach
    fun setUp() {
        siweAuthService = SiweAuthService(nonceService, tokenProvider, refreshTokenRepository, siweProps, jwtProps)
    }

    @Test
    fun `issueNonce should return nonce from nonceService`() {
        val expectedNonce = "test-nonce"
        `when`(nonceService.issueNonce()).thenReturn(expectedNonce)

        val response = siweAuthService.issueNonce()

        assertEquals(expectedNonce, response.nonce)
    }

    @Test
    fun `verify should succeed with valid request`() {
        val nonce = "valid-nonce"
        val now = Instant.now()
        val message = createSiweMessage(nonce, now)
        val signature = signMessage(message, ecKeyPair)

        `when`(nonceService.consumeNonce(nonce)).thenReturn(true)
        `when`(tokenProvider.createAccessToken(address.lowercase())).thenReturn("access-token")
        `when`(tokenProvider.createRefreshToken(address.lowercase())).thenReturn("refresh-token")

        val request = SiweVerifyRequest(message, signature)
        val response = siweAuthService.verify(request)

        assertEquals("access-token", response.accessToken)
        assertEquals("refresh-token", response.refreshToken)
        assertEquals(3600L, response.expiresIn)
    }

    @Test
    fun `verify should fail when domain is not allowed`() {
        val nonce = "valid-nonce"
        val now = Instant.now()
        val message = createSiweMessage(nonce, now, domain = "invalid.com")
        val signature = signMessage(message, ecKeyPair)

        val request = SiweVerifyRequest(message, signature)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            siweAuthService.verify(request)
        }
        assertEquals("Domain not allowed: invalid.com", exception.message)
    }

    @Test
    fun `verify should fail when chainId is not allowed`() {
        val nonce = "valid-nonce"
        val now = Instant.now()
        val message = createSiweMessage(nonce, now, chainId = 999L)
        val signature = signMessage(message, ecKeyPair)

        val request = SiweVerifyRequest(message, signature)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            siweAuthService.verify(request)
        }
        assertEquals("Chain ID not allowed", exception.message)
    }

    @Test
    fun `verify should fail when message is expired`() {
        val nonce = "valid-nonce"
        val now = Instant.now().minusSeconds(3600) // 1 hour ago
        val message = createSiweMessage(nonce, now, expirationTime = now.plusSeconds(1800)) // Expired 30 mins ago
        val signature = signMessage(message, ecKeyPair)

        val request = SiweVerifyRequest(message, signature)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            siweAuthService.verify(request)
        }
        assertEquals("Message expired", exception.message)
    }

    @Test
    fun `verify should fail when nonce is invalid or expired`() {
        val nonce = "invalid-nonce"
        val now = Instant.now()
        val message = createSiweMessage(nonce, now)
        val signature = signMessage(message, ecKeyPair)

        `when`(nonceService.consumeNonce(nonce)).thenReturn(false)

        val request = SiweVerifyRequest(message, signature)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            siweAuthService.verify(request)
        }
        assertEquals("Invalid/expired nonce", exception.message)
    }

    @Test
    fun `verify should fail when signature does not match address`() {
        val nonce = "valid-nonce"
        val now = Instant.now()
        val message = createSiweMessage(nonce, now)
        
        // Sign with a different key pair
        val otherKeyPair = Keys.createEcKeyPair()
        val signature = signMessage(message, otherKeyPair)

        `when`(nonceService.consumeNonce(nonce)).thenReturn(true)

        val request = SiweVerifyRequest(message, signature)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            siweAuthService.verify(request)
        }
        assertEquals("Signature does not match address", exception.message)
    }

    private fun createSiweMessage(
        nonce: String,
        issuedAt: Instant,
        domain: String = "example.com",
        chainId: Long = 1L,
        expirationTime: Instant? = null
    ): String {
        val sb = StringBuilder()
        sb.append("$domain wants you to sign in with your Ethereum account:\n")
        sb.append("$address\n\n")
        sb.append("URI: https://$domain\n")
        sb.append("Version: 1\n")
        sb.append("Chain ID: $chainId\n")
        sb.append("Nonce: $nonce\n")
        sb.append("Issued At: $issuedAt\n")
        if (expirationTime != null) {
            sb.append("Expiration Time: $expirationTime\n")
        }
        return sb.toString()
    }

    private fun signMessage(message: String, keyPair: ECKeyPair): String {
        val signatureData = Sign.signPrefixedMessage(message.toByteArray(), keyPair)
        val r = signatureData.r
        val s = signatureData.s
        val v = signatureData.v

        val sigBytes = ByteArray(65)
        System.arraycopy(r, 0, sigBytes, 0, 32)
        System.arraycopy(s, 0, sigBytes, 32, 32)
        sigBytes[64] = v[0]

        return Numeric.toHexString(sigBytes)
    }
}
