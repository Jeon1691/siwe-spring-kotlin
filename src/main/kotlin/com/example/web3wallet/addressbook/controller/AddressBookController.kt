package com.example.web3wallet.addressbook.controller

import com.example.web3wallet.addressbook.dto.AddressBookRequest
import com.example.web3wallet.addressbook.dto.AddressBookResponse
import com.example.web3wallet.addressbook.service.AddressBookService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Address Book", description = "지갑 주소록 관리 API")
@RestController
@RequestMapping("/api/address-book")
@SecurityRequirement(name = "Bearer Authentication")
class AddressBookController(
    private val service: AddressBookService
) {

    @Operation(summary = "주소록 추가", description = "새로운 주소를 주소록에 추가합니다.")
    @PostMapping
    fun create(
        @AuthenticationPrincipal userAddress: String,
        @RequestBody @Valid req: AddressBookRequest
    ): AddressBookResponse {
        return service.create(userAddress, req)
    }

    @Operation(summary = "주소록 조회", description = "내 주소록 목록을 조회합니다.")
    @GetMapping
    fun list(
        @AuthenticationPrincipal userAddress: String
    ): List<AddressBookResponse> {
        return service.list(userAddress)
    }

    @Operation(summary = "주소록 수정", description = "기존 주소록 항목을 수정합니다.")
    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal userAddress: String,
        @PathVariable id: Long,
        @RequestBody @Valid req: AddressBookRequest
    ): AddressBookResponse {
        return service.update(userAddress, id, req)
    }

    @Operation(summary = "즐겨찾기 토글", description = "주소록 항목의 즐겨찾기 상태를 변경합니다.")
    @PatchMapping("/{id}/favorite")
    fun toggleFavorite(
        @AuthenticationPrincipal userAddress: String,
        @PathVariable id: Long
    ): AddressBookResponse {
        return service.toggleFavorite(userAddress, id)
    }

    @Operation(summary = "주소록 삭제", description = "주소록 항목을 삭제합니다.")
    @DeleteMapping("/{id}")
    fun delete(
        @AuthenticationPrincipal userAddress: String,
        @PathVariable id: Long
    ) {
        service.delete(userAddress, id)
    }
}
