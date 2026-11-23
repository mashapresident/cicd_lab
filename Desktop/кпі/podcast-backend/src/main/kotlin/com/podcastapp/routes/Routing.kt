package com.podcastapp.routes

import com.podcastapp.dto.HealthDto
import com.podcastapp.errors.ValidationException
import com.podcastapp.services.AuthService
import com.podcastapp.services.CatalogService
import com.podcastapp.services.LibraryService
import com.podcastapp.services.RssImportService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.util.UUID
import io.ktor.server.application.call

fun Application.configureRouting(
    auth: AuthService,
    catalog: CatalogService,
    library: LibraryService,
    rss: RssImportService,
) {
    routing {
        get("/health") { call.respond(HealthDto("ok")) }
        route("/api/v1") {
            authRoutes(auth)
            catalogRoutes(catalog)
            authenticate("jwt") {
                meRoutes(auth, library, rss)
            }
        }
    }
}

fun ApplicationCall.uuidParam(name: String): UUID =
    parameters[name]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw ValidationException("Некоректний ідентифікатор")

fun ApplicationCall.pageParam(): Int = (request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)
fun ApplicationCall.sizeParam(): Int = (request.queryParameters["size"]?.toIntOrNull() ?: 20).coerceIn(1, 50)
