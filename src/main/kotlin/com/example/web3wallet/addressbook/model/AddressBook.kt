package com.example.web3wallet.addressbook.model

data class AddressBook(
    val id: Long,
    val ownerAddress: String, // The user who owns this address book entry
    val address: String,      // The address being saved
    val name: String,
    val memo: String?,
    val isFavorite: Boolean = false
)
