package com.example.web3wallet.addressbook.controller

import com.example.web3wallet.addressbook.dto.AddressBookRequest
import com.example.web3wallet.addressbook.dto.AddressBookResponse
import com.example.web3wallet.addressbook.service.AddressBookService
import com.example.web3wallet.auth.jwt.JwtFilter
import com.example.web3wallet.auth.jwt.TokenProvider
import com.example.web3wallet.config.SecurityConfig
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AddressBookController::class)
@Import(SecurityConfig::class, JwtFilter::class)
class AddressBookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var service: AddressBookService

    @MockitoBean
    private lateinit var tokenProvider: TokenProvider

    private val userAddress = "0x1234567890123456789012345678901234567890"
    private val auth = UsernamePasswordAuthenticationToken(userAddress, null, emptyList())

    @Test
    fun `create - should return created address book entry`() {
        val req = AddressBookRequest("0x1234567890123456789012345678901234567890", "My Friend", "Memo", isFavorite = true)
        val res = AddressBookResponse(1L, "0x1234567890123456789012345678901234567890", "My Friend", "Memo", isFavorite = true)

        `when`(service.create(userAddress, req)).thenReturn(res)

        mockMvc.perform(
            post("/api/address-book")
                .with(csrf())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("My Friend"))
            .andExpect(jsonPath("$.isFavorite").value(true))
    }

    @Test
    fun `list - should return list of entries`() {
        val list = listOf(
            AddressBookResponse(1L, "0xAddr1", "Name1", null, isFavorite = false),
            AddressBookResponse(2L, "0xAddr2", "Name2", "Memo2", isFavorite = true)
        )

        `when`(service.list(userAddress)).thenReturn(list)

        mockMvc.perform(
            get("/api/address-book")
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Name1"))
            .andExpect(jsonPath("$[1].isFavorite").value(true))
    }

    @Test
    fun `delete - should return 200 OK`() {
        mockMvc.perform(
            delete("/api/address-book/1")
                .with(csrf())
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
    }
}
