package com.jicay.bookmanagement.infrastructure.driving.web

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.usecase.BookUseCase
import com.jicay.bookmanagement.infrastructure.driving.web.exceptions.BookAlreadyReservedException
import com.jicay.bookmanagement.infrastructure.driving.web.exceptions.BookNotFoundException
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest
class BookControllerIT(
    @MockkBean private val bookUseCase: BookUseCase,
    private val mockMvc: MockMvc
) : FunSpec({
    extension(SpringExtension)

    test("rest route get books") {
        // GIVEN
        every { bookUseCase.getAllBooks() } returns listOf(Book("A", "B", true))

        // WHEN
        mockMvc.get("/books")
            //THEN
            .andExpect {
                status { isOk() }
                content { content { APPLICATION_JSON } }
                content {
                    json(
                        // language=json
                        """
                        [
                          {
                            "name": "A",
                            "author": "B",
                            "reserved": true
                          }
                        ]
                        """.trimIndent()
                    )
                }
            }
    }

    test("rest route post book") {
        justRun { bookUseCase.addBook(any()) }

        mockMvc.post("/books") {
            // language=json
            content = """
                {
                  "name": "Les misérables",
                  "author": "Victor Hugo",
                  "reserved": false
                }
            """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
        }

        val expected = Book(
            name = "Les misérables",
            author = "Victor Hugo",
            reserved = false
        )

        verify(exactly = 1) { bookUseCase.addBook(expected) }
    }

    test("rest route post book should return 400 when body is not good") {
        justRun { bookUseCase.addBook(any()) }

        mockMvc.post("/books") {
            // language=json
            content = """
                {
                  "title": "Les misérables",
                  "author": "Victor Hugo",
                  "reserved": false
                }
            """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { bookUseCase.addBook(any()) }
    }

    test("rest route reserve book") {
        val name = "Le Petit Prince"

        justRun { bookUseCase.reserveBook(any()) }

        mockMvc.post("/books/reserve?title=${name}")
            .andExpect {
                status { isOk() }
            }

        verify(exactly = 1) { bookUseCase.reserveBook(name) }
    }

    test("rest route reserve book should return error if book is not found with name") {
        val name = "unknown"

        every { bookUseCase.reserveBook(name) } throws BookNotFoundException("Book with name $name not found")

        mockMvc.post("/books/reserve?title=${name}")
            .andExpect {
                status { isNotFound() }
            }

        verify(exactly = 1) { bookUseCase.reserveBook(name) }
    }

    test("rest route reserve book should return error if book is not found with id") {
        val name = "unknown"
        val idBook = 999

        every { bookUseCase.reserveBook(name) } throws BookNotFoundException("Book with id $idBook not found")

        mockMvc.post("/books/reserve?title=${name}")
            .andExpect {
                status { isNotFound() }
            }

        verify(exactly = 1) { bookUseCase.reserveBook(name) }
    }

    test("rest route reserve book should return error if book is already reserved") {
        val idBook = 1
        val name = "Le Petit Prince"

        every { bookUseCase.reserveBook(name) } throws BookAlreadyReservedException("Book with ID $idBook and name $name is already reserved.")

        mockMvc.post("/books/reserve?title=${name}")
            .andExpect {
                status { isConflict() }
            }

        verify(exactly = 1) { bookUseCase.reserveBook(name) }
    }
})
