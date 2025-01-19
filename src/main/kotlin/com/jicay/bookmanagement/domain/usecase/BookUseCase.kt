package com.jicay.bookmanagement.domain.usecase

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import com.jicay.bookmanagement.infrastructure.driving.web.exceptions.BookAlreadyReservedException
import com.jicay.bookmanagement.infrastructure.driving.web.exceptions.BookNotFoundException

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
        val book = bookPort.getBook(id) ?: throw BookNotFoundException("Book not found")
        if (book.reserved) {
            throw BookAlreadyReservedException("Book with ID $id is already reserved.")
        }
        bookPort.updateBook(id, Book(name = book.name, author = book.author, reserved = true))
    }

    fun findBookById(id: Int): Book {
        return bookPort.getBook(id) ?: throw BookNotFoundException("Book not found")
    }
}
