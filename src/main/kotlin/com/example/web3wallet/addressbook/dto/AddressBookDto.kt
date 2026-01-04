package com.example.web3wallet.addressbook.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class AddressBookRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format")
    val address: String,

    @field:NotBlank
    val name: String,
    
    val memo: String? = null,
    
    val isFavorite: Boolean = false
)

data class AddressBookResponse(
    val id: Long,
    val address: String,
    val name: String,
    val memo: String?,
    val isFavorite: Boolean
)
