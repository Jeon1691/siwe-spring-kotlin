package com.example.web3wallet.auth.controller

import com.example.web3wallet.auth.dto.NonceResponse
import com.example.web3wallet.auth.dto.SiweVerifyRequest
import com.example.web3wallet.auth.dto.TokenResponse
import com.example.web3wallet.auth.service.SiweAuthService
import com.example.web3wallet.config.SecurityConfig
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SiweAuthController::class)
@Import(SecurityConfig::class)
class SiweAuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var siweAuthService: SiweAuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @WithMockUser
    fun `nonce endpoint should return nonce`() {
        val nonceResponse = NonceResponse("test-nonce")
        `when`(siweAuthService.issueNonce()).thenReturn(nonceResponse)

        mockMvc.perform(get("/api/auth/siwe/nonce"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nonce").value("test-nonce"))
    }

    @Test
    @WithMockUser
    fun `verify endpoint should return token response`() {
        val request = SiweVerifyRequest("message", "signature")
        val response = TokenResponse(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 3600
        )
        
        `when`(siweAuthService.verify(request)).thenReturn(response)

        mockMvc.perform(
            post("/api/auth/siwe/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.access_token").value("access-token"))
            .andExpect(jsonPath("$.refresh_token").value("refresh-token"))
            .andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(jsonPath("$.expires_in").value(3600))
    }
}
