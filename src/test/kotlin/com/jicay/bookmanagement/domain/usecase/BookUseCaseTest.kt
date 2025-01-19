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
        val idBook = 1
        val book = Book("Les Misérables", "Victor Hugo", false)

        every { bookPort.findBookByName(book.name) } returns idBook
        every { bookPort.findBookById(idBook) } returns book
        justRun { bookPort.updateBook(idBook, any()) }

        bookUseCase.reserveBook(book.name)

        verify(exactly = 1) { bookPort.findBookByName(book.name) }
        verify(exactly = 1) { bookPort.findBookById(idBook) }
        verify(exactly = 1) { bookPort.updateBook(idBook, Book(name = book.name, author = book.author, reserved = true)) }
    }

    test("reserve non-existing book with name should return error") {
        val name = "unknown"

        every { bookPort.findBookByName(name) } returns null

        shouldThrow<BookNotFoundException> {
            bookUseCase.reserveBook(name)
        }.apply {
            message shouldBe "Book with name $name not found"
        }

        verify(exactly = 1) { bookPort.findBookByName(name) }
    }

    test("reserve non-existing book with id should return error") {
        val name = "unknown"
        val idBook = 999

        every { bookPort.findBookByName(name) } returns idBook
        every { bookPort.findBookById(idBook) } returns null

        shouldThrow<BookNotFoundException> {
            bookUseCase.reserveBook(name)
        }.apply {
            message shouldBe "Book with id $idBook not found"
        }

        verify(exactly = 1) { bookPort.findBookByName(name) }
        verify(exactly = 1) { bookPort.findBookById(idBook) }
    }

    test("reserve reserved book should return error") {
        val book = Book("Les Misérables", "Victor Hugo", true)
        val idBook = 1

        every { bookPort.findBookByName(book.name) } returns idBook
        every { bookPort.findBookById(idBook) } returns book

        shouldThrow<BookAlreadyReservedException> {
            bookUseCase.reserveBook(book.name)
        }.apply {
            message shouldBe "Book with name ${book.name} and id $idBook is already reserved."
        }

        verify(exactly = 1) { bookPort.findBookByName(book.name) }
        verify(exactly = 1) { bookPort.findBookById(idBook) }
    }
})
