package com.example.web3wallet.auth.siwe

import java.time.Instant

/**
 * Very small EIP-4361 message parser.
 *
 * Expected shape (common format):
 *  <domain> wants you to sign in with your Ethereum account:
 *  <address>
 *
 *  <statement?>
 *
 *  URI: <uri>
 *  Version: 1
 *  Chain ID: <chainId>
 *  Nonce: <nonce>
 *  Issued At: <iso8601>
 *  Expiration Time: <iso8601>?
 *  Not Before: <iso8601>?
 *  Request ID: <id>?
 *  Resources:
 *  - <res1>
 *  - <res2>
 */
object SiweParser {

    fun parse(raw: String): SiweMessage {
        val lines = raw.replace("\r\n", "\n").split("\n")
        require(lines.size >= 6) { "SIWE message too short" }

        val first = lines[0].trim()
        val wants = " wants you to sign in with your Ethereum account:"
        require(first.contains(wants)) { "Invalid SIWE header line" }
        val domain = first.substringBefore(wants).trim()

        val address = lines[1].trim()
        require(address.startsWith("0x") && address.length >= 42) { "Invalid address line" }

        // Statement is optional: it's a block after an empty line.
        var idx = 2
        // skip blank lines
        while (idx < lines.size && lines[idx].isBlank()) idx++

        var statement: String? = null
        // If next non-blank line is not a "URI:" field, treat it as statement (can be multiple lines until blank)
        if (idx < lines.size && !lines[idx].startsWith("URI:")) {
            val stmtLines = mutableListOf<String>()
            while (idx < lines.size && lines[idx].isNotBlank()) {
                stmtLines += lines[idx]
                idx++
            }
            statement = stmtLines.joinToString("\n").trim().ifEmpty { null }
            while (idx < lines.size && lines[idx].isBlank()) idx++
        }

        val fields = linkedMapOf<String, String>()
        val resources = mutableListOf<String>()

        while (idx < lines.size) {
            val line = lines[idx]
            if (line.startsWith("Resources:")) {
                idx++
                while (idx < lines.size) {
                    val l = lines[idx].trim()
                    if (l.startsWith("- ")) resources += l.removePrefix("- ").trim()
                    idx++
                }
                break
            }

            val sep = line.indexOf(":")
            if (sep > 0) {
                val k = line.substring(0, sep).trim()
                val v = line.substring(sep + 1).trim()
                if (k.isNotEmpty()) fields[k] = v
            }
            idx++
        }

        fun req(name: String): String =
            fields[name] ?: throw IllegalArgumentException("Missing field: $name")

        val uri = req("URI")
        val version = req("Version")
        val chainId = req("Chain ID").toLongOrNull()
            ?: throw IllegalArgumentException("Invalid Chain ID")
        val nonce = req("Nonce")
        val issuedAt = Instant.parse(req("Issued At"))

        val expiration = fields["Expiration Time"]?.let { Instant.parse(it) }
        val notBefore = fields["Not Before"]?.let { Instant.parse(it) }
        val requestId = fields["Request ID"]

        return SiweMessage(
            domain = domain,
            address = address.lowercase(),
            statement = statement,
            uri = uri,
            version = version,
            chainId = chainId,
            nonce = nonce,
            issuedAt = issuedAt,
            expirationTime = expiration,
            notBefore = notBefore,
            requestId = requestId,
            resources = resources
        )
    }
}
