package com.jicay.bookmanagement.domain.model

data class Book(val name: String, val author: String) {
    init {
        require(name.isNotBlank()) { "Book name cannot be blank" }
        require(author.isNotBlank()) { "Book author cannot be blank" }
    }
}
