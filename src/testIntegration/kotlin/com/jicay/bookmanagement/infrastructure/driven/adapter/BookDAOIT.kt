package com.jicay.bookmanagement.infrastructure.driven.adapter

import com.jicay.bookmanagement.domain.model.Book
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.ResultSet

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookDAOIT(
    private val bookDAO: BookDAO
) : FunSpec() {
    init {
        extension(SpringExtension)

        beforeTest {
            performQuery(
                // language=sql
                "DELETE FROM book"
            )
        }

        test("get all books from db") {
            // GIVEN
            performQuery(
                // language=sql
                """
               insert into book (title, author, reserved)
               values 
                   ('Hamlet', 'Shakespeare', false),
                   ('Les fleurs du mal', 'Beaudelaire', false),
                   ('Harry Potter', 'Rowling', false);
            """.trimIndent()
            )

            // WHEN
            val res = bookDAO.getAllBooks()

            // THEN
            res.shouldContainExactlyInAnyOrder(
                Book("Hamlet", "Shakespeare", false),
                Book("Les fleurs du mal", "Beaudelaire", false),
                Book("Harry Potter", "Rowling", false)
            )
        }

        test("create book in db") {
            // GIVEN
            // WHEN
            bookDAO.createBook(Book("Les misérables", "Victor Hugo", false))

            // THEN
            val res = performQuery(
                // language=sql
                "SELECT * from book"
            )

            res shouldHaveSize 1
            assertSoftly(res.first()) {
                this["id"].shouldNotBeNull().shouldBeInstanceOf<Int>()
                this["title"].shouldBe("Les misérables")
                this["author"].shouldBe("Victor Hugo")
                this["reserved"].shouldBe(false)
            }
        }

        test("get book") {
            val id = 10

            // GIVEN
            performQuery(
                // language=sql
                """
               insert into book (id, title, author, reserved)
               values 
                ($id, 'Le Petit Prince', 'Saint-Exupéry', false),
                (1, 'Hamlet', 'Shakespeare', false),
                (2, 'Les fleurs du mal', 'Beaudelaire', false),
                (3, 'Harry Potter', 'Rowling', false);
               """.trimIndent()
            )

            // WHEN
            val res = bookDAO.getBook(id)

            // THEN
            res shouldBe Book("Le Petit Prince", "Saint-Exupéry", false)
        }

        test("get book with non-existing book") {
            // WHEN
            val res = bookDAO.getBook(999)

            // THEN
            res shouldBe null
        }

        test("update book") {
            val id = 1
            val newBook = Book("Le Petit Prince", "Conte d'Antoine de Saint-Exupéry", true)

            // GIVEN
            performQuery(
                // language=sql
                """
               insert into book (id, title, author, reserved)
               values 
                   ($id, 'Le Petit Prince', 'Saint-Exupéry', false);
               """.trimIndent()
            )

            // WHEN/THEN
            shouldNotThrowAny {
                bookDAO.updateBook(id, newBook)
            }
        }

        test("update book with non-existing book") {
            val id = 999 // non-existing id

            // WHEN/THEN
            shouldThrow<NoSuchElementException> {
                bookDAO.updateBook(id, Book("Les misérables", "Victor Hugo", false))
            }
        }

        afterSpec {
            container.stop()
        }
    }

    companion object {
        private val container = PostgreSQLContainer<Nothing>("postgres:13-alpine")

        init {
            container.start()
            System.setProperty("spring.datasource.url", container.jdbcUrl)
            System.setProperty("spring.datasource.username", container.username)
            System.setProperty("spring.datasource.password", container.password)
        }

        private fun ResultSet.toList(): List<Map<String, Any>> {
            val md = this.metaData
            val columns = md.columnCount
            val rows: MutableList<Map<String, Any>> = ArrayList()
            while (this.next()) {
                val row: MutableMap<String, Any> = HashMap(columns)
                for (i in 1..columns) {
                    row[md.getColumnName(i)] = this.getObject(i)
                }
                rows.add(row)
            }
            return rows
        }

        fun performQuery(sql: String): List<Map<String, Any>> {
            val hikariConfig = HikariConfig()
            hikariConfig.setJdbcUrl(container.jdbcUrl)
            hikariConfig.username = container.username
            hikariConfig.password = container.password
            hikariConfig.setDriverClassName(container.driverClassName)

            val ds = HikariDataSource(hikariConfig)

            val statement = ds.connection.createStatement()
            statement.execute(sql)
            val resultSet = statement.resultSet
            return resultSet?.toList() ?: listOf()
        }
    }
}
