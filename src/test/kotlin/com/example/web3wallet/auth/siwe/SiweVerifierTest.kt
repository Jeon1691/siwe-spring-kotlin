package com.example.web3wallet.auth.siwe

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

class SiweVerifierTest {

    @Test
    fun `recoverAddressFromPersonalSign should recover correct address`() {
        val keyPair = Keys.createEcKeyPair()
        val address = "0x" + Keys.getAddress(keyPair)
        val message = "Hello World"
        
        val signatureData = Sign.signPrefixedMessage(message.toByteArray(), keyPair)
        val r = signatureData.r
        val s = signatureData.s
        val v = signatureData.v

        val sigBytes = ByteArray(65)
        System.arraycopy(r, 0, sigBytes, 0, 32)
        System.arraycopy(s, 0, sigBytes, 32, 32)
        sigBytes[64] = v[0]
        
        val signatureHex = Numeric.toHexString(sigBytes)

        val recoveredAddress = SiweVerifier.recoverAddressFromPersonalSign(message, signatureHex)

        assertEquals(address.lowercase(), recoveredAddress.lowercase())
    }
}
