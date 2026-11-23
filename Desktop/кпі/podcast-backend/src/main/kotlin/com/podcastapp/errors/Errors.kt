package com.podcastapp.errors

import io.ktor.http.HttpStatusCode

open class ApiException(val status: HttpStatusCode, val code: String, message: String) : RuntimeException(message)

class NotFoundException(message: String = "Ресурс не знайдено") :
    ApiException(HttpStatusCode.NotFound, "NOT_FOUND", message)

class ConflictException(code: String, message: String) :
    ApiException(HttpStatusCode.Conflict, code, message)

class ValidationException(message: String) :
    ApiException(HttpStatusCode.BadRequest, "VALIDATION", message)

class UnauthorizedException(message: String) :
    ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", message)

class UnprocessableException(message: String) :
    ApiException(HttpStatusCode.UnprocessableEntity, "FEED_INVALID", message)
