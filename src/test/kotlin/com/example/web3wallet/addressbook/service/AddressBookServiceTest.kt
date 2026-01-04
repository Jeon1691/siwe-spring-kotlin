package com.example.web3wallet.addressbook.service

import com.example.web3wallet.addressbook.dto.AddressBookRequest
import com.example.web3wallet.addressbook.model.AddressBook
import com.example.web3wallet.addressbook.repository.AddressBookRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class AddressBookServiceTest {

    @Mock
    private lateinit var repository: AddressBookRepository

    @InjectMocks
    private lateinit var service: AddressBookService

    private val ownerAddress = "0x1234567890123456789012345678901234567890"
    private val targetAddress = "0x0987654321098765432109876543210987654321"

    @Test
    fun `create - should save and return response`() {
        // Given
        val req = AddressBookRequest(targetAddress, "My Friend", "Memo", isFavorite = true)
        val savedEntry = AddressBook(1L, ownerAddress, targetAddress, "My Friend", "Memo", isFavorite = true)

        `when`(repository.findByOwnerAndAddress(ownerAddress, targetAddress)).thenReturn(null)
        `when`(repository.save(ownerAddress, req.address, req.name, req.memo, req.isFavorite))
            .thenReturn(savedEntry)

        // When
        val result = service.create(ownerAddress, req)

        // Then
        assertEquals(1L, result.id)
        assertEquals(targetAddress, result.address)
        assertEquals("My Friend", result.name)
        assertEquals("Memo", result.memo)
        assertTrue(result.isFavorite)
    }

    @Test
    fun `create - should throw exception if address already exists`() {
        // Given
        val req = AddressBookRequest(targetAddress, "My Friend", "Memo")
        val existingEntry = AddressBook(1L, ownerAddress, targetAddress, "Existing", "Memo", isFavorite = false)

        `when`(repository.findByOwnerAndAddress(ownerAddress, targetAddress)).thenReturn(existingEntry)

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.create(ownerAddress, req)
        }
        assertEquals("Address already exists in address book", exception.message)
    }

    @Test
    fun `list - should return list of address books sorted by favorite`() {
        // Given
        val entries = listOf(
            AddressBook(1L, ownerAddress, targetAddress, "Friend 1", null, isFavorite = false),
            AddressBook(2L, ownerAddress, "0xAnotherAddress", "Friend 2", "Memo 2", isFavorite = true)
        )
        `when`(repository.findAllByOwner(ownerAddress)).thenReturn(entries)

        // When
        val result = service.list(ownerAddress)

        // Then
        assertEquals(2, result.size)
        // Favorite one should come first
        assertEquals("Friend 2", result[0].name)
        assertTrue(result[0].isFavorite)
        
        assertEquals("Friend 1", result[1].name)
        assertFalse(result[1].isFavorite)
    }

    @Test
    fun `update - should update existing entry`() {
        // Given
        val req = AddressBookRequest(targetAddress, "Updated Name", "Updated Memo", isFavorite = true)
        val updatedEntry = AddressBook(1L, ownerAddress, targetAddress, "Updated Name", "Updated Memo", isFavorite = true)

        `when`(repository.findByOwnerAndAddress(ownerAddress, targetAddress)).thenReturn(null)
        `when`(repository.update(1L, ownerAddress, req.address, req.name, req.memo, req.isFavorite))
            .thenReturn(updatedEntry)

        // When
        val result = service.update(ownerAddress, 1L, req)

        // Then
        assertEquals("Updated Name", result.name)
        assertEquals("Updated Memo", result.memo)
        assertTrue(result.isFavorite)
    }

    @Test
    fun `update - should throw exception if duplicate address found`() {
        // Given
        val req = AddressBookRequest("0xDuplicateAddress", "Updated Name", "Updated Memo")
        val duplicateEntry = AddressBook(2L, ownerAddress, "0xDuplicateAddress", "Duplicate", "Memo", isFavorite = false)

        // Trying to update ID 1 to have the same address as ID 2
        `when`(repository.findByOwnerAndAddress(ownerAddress, req.address)).thenReturn(duplicateEntry)

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.update(ownerAddress, 1L, req)
        }
        assertEquals("Address already exists in address book", exception.message)
    }

    @Test
    fun `update - should throw exception if not found`() {
        // Given
        val req = AddressBookRequest(targetAddress, "Updated Name", null)
        `when`(repository.findByOwnerAndAddress(ownerAddress, targetAddress)).thenReturn(null)
        `when`(repository.update(99L, ownerAddress, req.address, req.name, req.memo, req.isFavorite))
            .thenReturn(null)

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            service.update(ownerAddress, 99L, req)
        }
    }

    @Test
    fun `delete - should delete entry`() {
        // Given
        `when`(repository.delete(1L, ownerAddress)).thenReturn(true)

        // When
        service.delete(ownerAddress, 1L)

        // Then
        verify(repository).delete(1L, ownerAddress)
    }

    @Test
    fun `delete - should throw exception if not found`() {
        // Given
        `when`(repository.delete(99L, ownerAddress)).thenReturn(false)

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            service.delete(ownerAddress, 99L)
        }
    }
}
