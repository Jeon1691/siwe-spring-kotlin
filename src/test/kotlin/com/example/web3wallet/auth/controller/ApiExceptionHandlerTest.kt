package com.example.web3wallet.auth.controller

import com.example.web3wallet.auth.dto.SiweVerifyRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SiweAuthController::class)
@Import(SecurityConfig::class)
class ApiExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var siweAuthService: SiweAuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @WithMockUser
    fun `should handle IllegalArgumentException`() {
        val request = SiweVerifyRequest("message", "signature")
        `when`(siweAuthService.verify(request)).thenThrow(IllegalArgumentException("Custom error message"))

        mockMvc.perform(
            post("/api/auth/siwe/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Custom error message"))
    }

    @Test
    @WithMockUser
    fun `should handle MethodArgumentNotValidException`() {
        // Sending invalid request (empty message and signature)
        val request = SiweVerifyRequest("", "")

        mockMvc.perform(
            post("/api/auth/siwe/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            // The error message might contain field details now, so we check if it contains "Validation failed"
            .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Validation failed")))
    }
}
