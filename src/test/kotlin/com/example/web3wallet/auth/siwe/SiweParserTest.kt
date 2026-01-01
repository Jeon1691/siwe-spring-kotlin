package com.example.web3wallet.auth.siwe

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class SiweParserTest {

    @Test
    fun `parse should correctly parse valid SIWE message`() {
        val message = """
            example.com wants you to sign in with your Ethereum account:
            0x31b26e43651e9371c88af3d36c14cfd938bbf4ae

            Sign in with Ethereum to the app.

            URI: https://example.com
            Version: 1
            Chain ID: 1
            Nonce: 32891756
            Issued At: 2021-09-30T16:25:24Z
            Resources:
            - ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3ev27uec3i
            - https://example.com/my-resource
        """.trimIndent()

        val siweMessage = SiweParser.parse(message)

        assertEquals("example.com", siweMessage.domain)
        assertEquals("0x31b26e43651e9371c88af3d36c14cfd938bbf4ae", siweMessage.address)
        assertEquals("Sign in with Ethereum to the app.", siweMessage.statement)
        assertEquals("https://example.com", siweMessage.uri)
        assertEquals("1", siweMessage.version)
        assertEquals(1L, siweMessage.chainId)
        assertEquals("32891756", siweMessage.nonce)
        assertEquals(Instant.parse("2021-09-30T16:25:24Z"), siweMessage.issuedAt)
        assertEquals(2, siweMessage.resources.size)
        assertEquals("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3ev27uec3i", siweMessage.resources[0])
        assertEquals("https://example.com/my-resource", siweMessage.resources[1])
    }

    @Test
    fun `parse should fail with invalid address`() {
        val message = """
            example.com wants you to sign in with your Ethereum account:
            invalid-address

            URI: https://example.com
            Version: 1
            Chain ID: 1
            Nonce: 32891756
            Issued At: 2021-09-30T16:25:24Z
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            SiweParser.parse(message)
        }
    }

    @Test
    fun `parse should fail with missing required fields`() {
        val message = """
            example.com wants you to sign in with your Ethereum account:
            0x31b26e43651e9371c88af3d36c14cfd938bbf4ae

            URI: https://example.com
            Version: 1
            Chain ID: 1
            Issued At: 2021-09-30T16:25:24Z
        """.trimIndent() // Missing Nonce

        assertThrows(IllegalArgumentException::class.java) {
            SiweParser.parse(message)
        }
    }
}
