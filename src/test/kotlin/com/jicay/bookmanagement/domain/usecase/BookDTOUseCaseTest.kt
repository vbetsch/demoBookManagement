package com.jicay.bookmanagement.domain.usecase

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import net.jqwik.api.*
import net.jqwik.api.Combinators.combine
import net.jqwik.api.lifecycle.AfterProperty
import net.jqwik.api.lifecycle.BeforeProperty
import net.jqwik.kotlin.api.any
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*


@ExtendWith(MockKExtension::class)
class BookDTOUseCaseTest {

    @InjectMockKs
    private lateinit var bookUseCase: BookUseCase

    @MockK
    private lateinit var bookPort: BookPort

    @Test
    fun `get all books should returns all books sorted by name`() {
        every { bookPort.getAllBooks() } returns listOf(
            Book("Les Misérables", "Victor Hugo"),
            Book("Hamlet", "William Shakespeare")
        )

        val res = bookUseCase.getAllBooks()

        assertThat(res).containsExactly(
            Book("Hamlet", "William Shakespeare"),
            Book("Les Misérables", "Victor Hugo")
        )
    }

    @Test
    fun `add book`() {
        justRun { bookPort.createBook(any()) }

        val book = Book("Les Misérables", "Victor Hugo")

        bookUseCase.addBook(book)

        verify(exactly = 1) { bookPort.createBook(book) }
    }

    @BeforeProperty
    fun initMocks() {
        MockKAnnotations.init(this)
    }

    @Property
    fun `get all all book should have all books stored in db`(
        @ForAll("bookGenerator") books: List<Book>
    ) {
        every { bookPort.getAllBooks() } returns books

        val res = bookUseCase.getAllBooks()

        assertThat(res).containsExactlyInAnyOrder(*books.toTypedArray())
    }

    @Provide
    fun bookGenerator(): Arbitrary<List<Book>> {
        return combine(
                String.any().ofMinLength(1).ofMaxLength(20).alpha(),
                String.any().ofMinLength(1).ofMaxLength(20).alpha()).`as` { title: String, author: String ->
            Book(title, author)
        }.list().uniqueElements().ofSize(10)
    }
}