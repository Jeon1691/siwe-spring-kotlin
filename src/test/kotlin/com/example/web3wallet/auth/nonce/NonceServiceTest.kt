package com.example.web3wallet.auth.nonce

import com.example.web3wallet.config.SiweProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NonceServiceTest {

    private lateinit var nonceService: NonceService
    private val props = SiweProperties(nonceTtlMinutes = 1)

    @BeforeEach
    fun setUp() {
        nonceService = NonceService(props)
    }

    @Test
    fun `issueNonce should return a non-empty string`() {
        val nonce = nonceService.issueNonce()
        assertNotNull(nonce)
        assertTrue(nonce.isNotEmpty())
    }

    @Test
    fun `consumeNonce should return true for valid nonce`() {
        val nonce = nonceService.issueNonce()
        assertTrue(nonceService.consumeNonce(nonce))
    }

    @Test
    fun `consumeNonce should return false for already consumed nonce`() {
        val nonce = nonceService.issueNonce()
        nonceService.consumeNonce(nonce)
        assertFalse(nonceService.consumeNonce(nonce))
    }

    @Test
    fun `consumeNonce should return false for non-existent nonce`() {
        assertFalse(nonceService.consumeNonce("non-existent-nonce"))
    }
}
