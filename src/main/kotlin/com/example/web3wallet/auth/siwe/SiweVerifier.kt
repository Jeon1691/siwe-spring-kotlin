package com.example.web3wallet.auth.siwe

import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigInteger

object SiweVerifier {

    /**
     * Verifies a `personal_sign` style signature for the given raw message.
     * Returns the recovered address (lowercase, 0x...) if valid, otherwise throws.
     */
    fun recoverAddressFromPersonalSign(message: String, signatureHex: String): String {
        val sigBytes = Numeric.hexStringToByteArray(signatureHex)
        require(sigBytes.size == 65) { "Signature must be 65 bytes" }

        val r = sigBytes.copyOfRange(0, 32)
        val s = sigBytes.copyOfRange(32, 64)
        var v: Byte = sigBytes[64]

        // Normalize v (MetaMask often returns 27/28; some libs use 0/1)
        if (v.toInt() < 27) v = (v + 27).toByte()

        val sigData = Sign.SignatureData(v, r, s)
        val pubKey: BigInteger = Sign.signedPrefixedMessageToKey(
            message.toByteArray(Charsets.UTF_8),
            sigData
        )
        val addrNo0x = Keys.getAddress(pubKey)
        return "0x${addrNo0x.lowercase()}"
    }
}
