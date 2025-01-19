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

    fun reserveBook(name: String) {
        val id = bookPort.findBookByName(name) ?: throw BookNotFoundException("Book with name $name not found")
        val book = bookPort.findBookById(id) ?: throw BookNotFoundException("Book with id $id not found")
        if (book.reserved) {
            throw BookAlreadyReservedException("Book with name $name and id $id is already reserved.")
        }
        bookPort.updateBook(id, Book(name = book.name, author = book.author, reserved = true))
    }
}
