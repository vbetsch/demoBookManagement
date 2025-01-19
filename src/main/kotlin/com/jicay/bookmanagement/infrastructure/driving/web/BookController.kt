package com.jicay.bookmanagement.infrastructure.driving.web

import com.jicay.bookmanagement.domain.usecase.BookUseCase
import com.jicay.bookmanagement.infrastructure.driving.web.dto.BookDTO
import com.jicay.bookmanagement.infrastructure.driving.web.dto.toDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

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
    fun reserveBookById(@PathVariable id: Int) {
        bookUseCase.reserveBookById(id)
    }

    @CrossOrigin
    @PostMapping("/reserve")
    fun reserveBookByName(@RequestParam("name") name: String) {
        bookUseCase.reserveBookByName(name)
    }
}
