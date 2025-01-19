package com.jicay.bookmanagement.domain.usecase

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

class InMemoryBookPort : BookPort {
    private val books = mutableListOf<Book>()
    private val id = 1
    private var book: Book = Book("Les Misérables", "Victor Hugo", false)

    override fun getAllBooks(): List<Book> = books

    override fun createBook(book: Book) {
        books.add(book)
    }

    override fun findBookByName(name: String): Int? = id

    override fun findBookById(id: Int): Book = book

    override fun updateBook(id: Int, data: Book) {
        book = Book(data.name, data.author, data.reserved)
    }

    fun clear() {
        books.clear()
    }
}

class BookUseCasePropertyTest : FunSpec({

    val bookPort = InMemoryBookPort()
    val bookUseCase = BookUseCase(bookPort)

    test("should return all elements in the alphabetical order") {
        checkAll(Arb.int(1..100)) { nbItems ->
            bookPort.clear()

            val arb = Arb.stringPattern("""[a-z]{1,10}""")

            val titles = mutableListOf<String>()

            for (i in 1..nbItems) {
                val title = arb.next()
                titles.add(title)
                bookUseCase.addBook(Book(title, "Victor Hugo", false))
            }

            val res = bookUseCase.getAllBooks()

            res.map { it.name } shouldContainExactly titles.sorted()
        }
    }
})
