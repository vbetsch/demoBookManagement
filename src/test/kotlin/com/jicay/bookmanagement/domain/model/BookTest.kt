package com.jicay.bookmanagement.domain.model

import assertk.assertFailure
import assertk.assertions.hasMessage
import assertk.assertions.isInstanceOf
import org.junit.jupiter.api.Test

class BookTest {
    @Test
    fun `book name cannot be blank`() {
        assertFailure { Book("", "Victor Hugo") }
            .isInstanceOf(IllegalArgumentException::class)
            .hasMessage("Book name cannot be blank")
    }

    @Test
    fun `book author cannot be blank`() {
        assertFailure { Book("Les misérables", "") }
            .isInstanceOf(IllegalArgumentException::class)
            .hasMessage("Book author cannot be blank")
    }
}