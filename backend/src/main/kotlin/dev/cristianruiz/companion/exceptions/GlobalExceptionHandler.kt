package dev.cristianruiz.companion.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

data class ErrorResponse(val message: String)

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {

        val error = ErrorResponse(ex.message ?: "Not found")
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(ex.message ?: "Bad request")
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

}

class BadRequestException(msg: String) : RuntimeException(msg)