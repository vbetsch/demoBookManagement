package com.jicay.bookmanagement.domain.usecase

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import com.jicay.bookmanagement.infrastructure.driving.web.exceptions.BookAlreadyReservedException

class BookUseCase(
    private val bookPort: BookPort
) {
    fun getAllBooks(): List<Book> {
        return bookPort.getAllBooks().sortedBy {
            it.name.lowercase()
        }
    }

    fun addBook(book: Book) {
        bookPort.createBook(book)
    }

    fun reserveBook(id: Int) {
        var book = bookPort.getBook(id)
        if (book.reserved) {
            throw BookAlreadyReservedException("Book with ID $id is already reserved.")
        }
        bookPort.updateBook(id, Book(name = book.name, author = book.author, reserved = true))
    }
}
