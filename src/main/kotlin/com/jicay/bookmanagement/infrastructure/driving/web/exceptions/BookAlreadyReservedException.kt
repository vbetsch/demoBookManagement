package com.jicay.bookmanagement.infrastructure.driving.web.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class BookAlreadyReservedException(message: String) : RuntimeException(message)
