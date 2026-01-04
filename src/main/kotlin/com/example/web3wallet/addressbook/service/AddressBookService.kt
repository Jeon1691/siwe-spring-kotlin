package com.example.web3wallet.addressbook.service

import com.example.web3wallet.addressbook.dto.AddressBookRequest
import com.example.web3wallet.addressbook.dto.AddressBookResponse
import com.example.web3wallet.addressbook.repository.AddressBookRepository
import org.springframework.stereotype.Service

@Service
class AddressBookService(
    private val repository: AddressBookRepository
) {

    fun create(ownerAddress: String, req: AddressBookRequest): AddressBookResponse {
        // Check for duplicate address
        val existing = repository.findByOwnerAndAddress(ownerAddress, req.address)
        if (existing != null) {
            throw IllegalArgumentException("Address already exists in address book")
        }

        val saved = repository.save(ownerAddress, req.address, req.name, req.memo, req.isFavorite)
        return AddressBookResponse(saved.id, saved.address, saved.name, saved.memo, saved.isFavorite)
    }

    fun list(ownerAddress: String): List<AddressBookResponse> {
        return repository.findAllByOwner(ownerAddress)
            .sortedWith(compareByDescending<com.example.web3wallet.addressbook.model.AddressBook> { it.isFavorite }
                .thenBy { it.name }) // Sort by favorite first, then by name
            .map {
                AddressBookResponse(it.id, it.address, it.name, it.memo, it.isFavorite)
            }
    }

    fun update(ownerAddress: String, id: Long, req: AddressBookRequest): AddressBookResponse {
        // Check if address is being changed to one that already exists (excluding self)
        val duplicate = repository.findByOwnerAndAddress(ownerAddress, req.address)
        if (duplicate != null && duplicate.id != id) {
            throw IllegalArgumentException("Address already exists in address book")
        }

        val updated = repository.update(id, ownerAddress, req.address, req.name, req.memo, req.isFavorite)
            ?: throw IllegalArgumentException("Address book entry not found")
        return AddressBookResponse(updated.id, updated.address, updated.name, updated.memo, updated.isFavorite)
    }

    fun toggleFavorite(ownerAddress: String, id: Long): AddressBookResponse {
        val existing = repository.findByIdAndOwner(id, ownerAddress)
            ?: throw IllegalArgumentException("Address book entry not found")
        
        val newFavoriteStatus = !existing.isFavorite
        val updated = repository.update(id, ownerAddress, existing.address, existing.name, existing.memo, newFavoriteStatus)
            ?: throw IllegalArgumentException("Failed to update favorite status")

        return AddressBookResponse(updated.id, updated.address, updated.name, updated.memo, updated.isFavorite)
    }

    fun delete(ownerAddress: String, id: Long) {
        if (!repository.delete(id, ownerAddress)) {
            throw IllegalArgumentException("Address book entry not found")
        }
    }
}
