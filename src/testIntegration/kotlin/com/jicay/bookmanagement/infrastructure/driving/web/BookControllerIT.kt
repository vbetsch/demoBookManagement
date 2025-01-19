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
        val id = 1

        justRun { bookUseCase.reserveBook(any()) }

        mockMvc.post("/books/${id}/reserve")
            .andExpect {
                status { isOk() }
            }

        verify(exactly = 1) { bookUseCase.reserveBook(id) }
    }

    test("rest route reserve book should return error if book is not found") {
        val id = 999

        every { bookUseCase.reserveBook(id) } throws BookNotFoundException("Book with id $id not found")

        mockMvc.post("/books/${id}/reserve")
            .andExpect {
                status { isNotFound() }
            }

        verify(exactly = 1) { bookUseCase.reserveBook(id) }
    }

    test("rest route reserve book should return error if book is already reserved") {
        val id = 1

        every { bookUseCase.reserveBook(id) } throws BookAlreadyReservedException("Book with ID $id is already reserved.")

        mockMvc.post("/books/${id}/reserve")
            .andExpect {
                status { isConflict() }
            }

        verify(exactly = 1) { bookUseCase.reserveBook(id) }
    }
})
