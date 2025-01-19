package com.jicay.bookmanagement.domain.usecase

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import com.jicay.bookmanagement.infrastructure.driving.web.exceptions.BookAlreadyReservedException
import com.jicay.bookmanagement.infrastructure.driving.web.exceptions.BookNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.*

class BookUseCaseTest : FunSpec({

    val bookPort = mockk<BookPort>()
    val bookUseCase = BookUseCase(bookPort)

    beforeTest {
        clearMocks(bookPort)
    }

    test("get all books should returns all books sorted by name") {
        every { bookPort.getAllBooks() } returns listOf(
            Book("Les Misérables", "Victor Hugo", false),
            Book("Hamlet", "William Shakespeare", false)
        )

        val res = bookUseCase.getAllBooks()

        res.shouldContainExactly(
            Book("Hamlet", "William Shakespeare", false),
            Book("Les Misérables", "Victor Hugo", false)
        )
    }

    test("add book") {
        justRun { bookPort.createBook(any()) }

        val book = Book("Les Misérables", "Victor Hugo", false)

        bookUseCase.addBook(book)

        verify(exactly = 1) { bookPort.createBook(book) }
    }

    test("reserve book") {
        val book = Book("Les Misérables", "Victor Hugo", false)

        every { bookPort.getBook(any()) } returns book
        justRun { bookPort.updateBook(any(), any()) }

        val id = 1

        bookUseCase.reserveBook(id)

        verify(exactly = 1) { bookPort.getBook(id) }
        verify(exactly = 1) { bookPort.updateBook(id, Book(name = book.name, author = book.author, reserved = true)) }
    }

    test("reserve non-existing book should return error") {
        val id = 999

        every { bookPort.getBook(id) } returns null

        shouldThrow<BookNotFoundException> {
            bookUseCase.reserveBook(id)
        }.apply {
            message shouldBe "Book with id $id not found"
        }

        verify(exactly = 1) { bookPort.getBook(id) }
    }

    test("reserve reserved book should return error") {
        val book = Book("Les Misérables", "Victor Hugo", true)
        val id = 1

        every { bookPort.getBook(id) } returns book

        shouldThrow<BookAlreadyReservedException> {
            bookUseCase.reserveBook(id)
        }.apply {
            message shouldBe "Book with ID $id is already reserved."
        }

        verify(exactly = 1) { bookPort.getBook(id) }
    }
})
