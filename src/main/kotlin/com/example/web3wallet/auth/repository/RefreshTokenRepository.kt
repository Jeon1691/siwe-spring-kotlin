package com.example.web3wallet.auth.repository

import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class RefreshTokenRepository {
    // Key: RefreshToken, Value: Address (Subject)
    private val store = ConcurrentHashMap<String, String>()

    fun save(refreshToken: String, address: String) {
        store[refreshToken] = address
    }

    fun findByToken(refreshToken: String): String? {
        return store[refreshToken]
    }

    fun delete(refreshToken: String) {
        store.remove(refreshToken)
    }
}
