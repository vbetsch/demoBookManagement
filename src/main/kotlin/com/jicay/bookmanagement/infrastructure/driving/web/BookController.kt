package com.jicay.bookmanagement.infrastructure.driving.web

import com.jicay.bookmanagement.domain.usecase.BookUseCase
import com.jicay.bookmanagement.infrastructure.driving.web.dto.BookDTO
import com.jicay.bookmanagement.infrastructure.driving.web.dto.toDto
import com.jicay.bookmanagement.infrastructure.driving.web.exceptions.BookAlreadyReservedException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/books")
class BookController(
    private val bookUseCase: BookUseCase
) {
    @CrossOrigin
    @GetMapping
    fun getAllBooks(): List<BookDTO> {
        return bookUseCase.getAllBooks()
            .map { it.toDto() }
    }

    @CrossOrigin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addBook(@RequestBody bookDTO: BookDTO) {
        bookUseCase.addBook(bookDTO.toDomain())
    }

    @CrossOrigin
    @PostMapping("/{id}/reserve")
    @ResponseStatus(HttpStatus.OK)
    fun reserveBook(@PathVariable id: Int) {
        try {
            bookUseCase.reserveBook(id)
        } catch (e: BookAlreadyReservedException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, e.message, e)
        }
    }
}
