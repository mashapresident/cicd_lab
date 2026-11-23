package com.podcastapp.plugins

import com.podcastapp.dto.ErrorDto
import com.podcastapp.errors.ApiException
import com.podcastapp.security.JwtService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

val AUTH_RATE_LIMIT = RateLimitName("auth")

fun Application.configurePlugins(jwt: JwtService) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
    }
    install(CallLogging)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }
    install(RateLimit) {
        register(AUTH_RATE_LIMIT) { rateLimiter(limit = 20, refillPeriod = 60.seconds) }
    }
    install(Authentication) {
        jwt("jwt") {
            verifier(jwt.verifier)
            validate { cred -> if (cred.payload.subject != null) JWTPrincipal(cred.payload) else null }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorDto("UNAUTHORIZED", "Потрібна авторизація"))
            }
        }
    }
    install(StatusPages) {
        exception<ApiException> { call, e ->
            call.respond(e.status, ErrorDto(e.code, e.message ?: "Помилка"))
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorDto("VALIDATION", "Некоректне тіло запиту"))
        }
        exception<SerializationException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorDto("VALIDATION", "Некоректний JSON"))
        }
        exception<Throwable> { call, e ->
            call.application.log.error("Unhandled error", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorDto("INTERNAL", "Внутрішня помилка сервера"))
        }
    }
}
