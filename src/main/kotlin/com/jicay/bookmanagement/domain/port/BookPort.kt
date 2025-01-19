package com.jicay.bookmanagement.domain.port

import com.jicay.bookmanagement.domain.model.Book

interface BookPort {
    fun getAllBooks(): List<Book>
    fun createBook(book: Book)
    fun findBookByName(name: String): Int?
    fun findBookById(id: Int): Book?
    fun updateBook(id: Int, data: Book)
}
