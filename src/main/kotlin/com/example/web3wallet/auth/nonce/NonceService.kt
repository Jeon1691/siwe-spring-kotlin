package com.example.web3wallet.auth.nonce

import com.example.web3wallet.config.SiweProperties
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

@Service
class NonceService(
    private val props: SiweProperties
) {
    private val rng = SecureRandom()
    private val store = ConcurrentHashMap<String, Instant>()

    private fun ttl(): Duration = Duration.ofMinutes(props.nonceTtlMinutes)

    fun issueNonce(): String {
        val buf = ByteArray(16)
        rng.nextBytes(buf)
        val nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(buf)
        store[nonce] = Instant.now().plus(ttl())
        return nonce
    }

    /**
     * One-time consume. Returns true iff exists and not expired.
     */
    fun consumeNonce(nonce: String): Boolean {
        val exp = store.remove(nonce) ?: return false
        return Instant.now().isBefore(exp)
    }
}
