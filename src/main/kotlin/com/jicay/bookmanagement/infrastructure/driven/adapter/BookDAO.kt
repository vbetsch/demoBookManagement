package com.jicay.bookmanagement.infrastructure.driven.adapter

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

@Service
class BookDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : BookPort {

    override fun getAllBooks(): List<Book> {
        return namedParameterJdbcTemplate
            .query("SELECT * FROM BOOK", MapSqlParameterSource()) { rs, _ ->
                Book(
                    name = rs.getString("title"),
                    author = rs.getString("author"),
                    reserved = rs.getBoolean("reserved")
                )
            }
    }

    override fun createBook(book: Book) {
        namedParameterJdbcTemplate
            .update(
                "INSERT INTO BOOK (title, author, reserved) values (:title, :author, :reserved)", mapOf(
                    "title" to book.name,
                    "author" to book.author,
                    "reserved" to book.reserved
                )
            )
    }

    override fun getBook(id: Int): Book? {
        return try {
            namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM BOOK WHERE id = :id",
                MapSqlParameterSource().addValue("id", id)
            ) { rs, _ ->
                Book(
                    name = rs.getString("title"),
                    author = rs.getString("author"),
                    reserved = rs.getBoolean("reserved")
                )
            }
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    override fun updateBook(id: Int, data: Book) {
        val rowsAffected = namedParameterJdbcTemplate.update(
            """
                UPDATE BOOK 
                SET title = :title, author = :author, reserved = :reserved 
                WHERE id = :id
            """,
            MapSqlParameterSource()
                .addValue("id", id)
                .addValue("title", data.name)
                .addValue("author", data.author)
                .addValue("reserved", data.reserved)
        )

        if (rowsAffected == 0) {
            throw NoSuchElementException("Book not found with id $id")
        }
    }
}
