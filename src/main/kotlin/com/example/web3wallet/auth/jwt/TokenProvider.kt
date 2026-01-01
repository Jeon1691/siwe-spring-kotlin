package com.example.web3wallet.auth.jwt

import com.example.web3wallet.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class TokenProvider(
    private val props: JwtProperties
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(props.secret.toByteArray())

    fun createAccessToken(subject: String): String {
        val now = Date()
        val validity = Date(now.time + props.accessTokenValiditySeconds * 1000)

        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(subject: String): String {
        val now = Date()
        val validity = Date(now.time + props.refreshTokenValiditySeconds * 1000)

        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseClaims(token)
            true
        } catch (_: Exception) {
            false
        }
    }

    // Used for future implementation or testing
    @Suppress("unused")
    fun getSubject(token: String): String {
        return parseClaims(token).subject
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
