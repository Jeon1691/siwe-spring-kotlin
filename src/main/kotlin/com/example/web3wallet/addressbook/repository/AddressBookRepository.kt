package com.example.web3wallet.addressbook.repository

import com.example.web3wallet.addressbook.model.AddressBook
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Repository
class AddressBookRepository {
    // Key: ID, Value: AddressBook
    private val store = ConcurrentHashMap<Long, AddressBook>()
    private val idGenerator = AtomicLong(1)

    fun save(ownerAddress: String, address: String, name: String, memo: String?, isFavorite: Boolean): AddressBook {
        val id = idGenerator.getAndIncrement()
        val entry = AddressBook(id, ownerAddress, address, name, memo, isFavorite)
        store[id] = entry
        return entry
    }

    fun findAllByOwner(ownerAddress: String): List<AddressBook> {
        return store.values.filter { it.ownerAddress.equals(ownerAddress, ignoreCase = true) }
    }

    fun findByIdAndOwner(id: Long, ownerAddress: String): AddressBook? {
        val entry = store[id] ?: return null
        return if (entry.ownerAddress.equals(ownerAddress, ignoreCase = true)) entry else null
    }

    fun findByOwnerAndAddress(ownerAddress: String, address: String): AddressBook? {
        return store.values.find { 
            it.ownerAddress.equals(ownerAddress, ignoreCase = true) && 
            it.address.equals(address, ignoreCase = true) 
        }
    }

    fun update(id: Long, ownerAddress: String, address: String, name: String, memo: String?, isFavorite: Boolean): AddressBook? {
        val existing = findByIdAndOwner(id, ownerAddress) ?: return null
        val updated = existing.copy(address = address, name = name, memo = memo, isFavorite = isFavorite)
        store[id] = updated
        return updated
    }

    fun delete(id: Long, ownerAddress: String): Boolean {
        val existing = findByIdAndOwner(id, ownerAddress) ?: return false
        store.remove(id)
        return true
    }
}
