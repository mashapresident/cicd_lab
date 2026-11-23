package com.podcastapp.routes

import com.podcastapp.services.CatalogService
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.catalogRoutes(catalog: CatalogService) {
    get("/categories") { call.respond(catalog.categories()) }

    get("/podcasts") {
        val q = call.request.queryParameters["query"]
        val category = call.request.queryParameters["category"]?.toIntOrNull()
        call.respond(catalog.search(q, category, call.pageParam(), call.sizeParam()))
    }
    get("/podcasts/{id}") { call.respond(catalog.podcast(call.uuidParam("id"))) }
    get("/podcasts/{id}/episodes") {
        call.respond(catalog.episodes(call.uuidParam("id"), call.pageParam(), call.sizeParam()))
    }
    get("/episodes/{id}") { call.respond(catalog.episode(call.uuidParam("id"))) }
}
