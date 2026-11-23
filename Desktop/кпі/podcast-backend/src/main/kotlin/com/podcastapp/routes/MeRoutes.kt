package com.podcastapp.routes

import com.podcastapp.dto.ImportRequest
import com.podcastapp.dto.ProgressRequest
import com.podcastapp.security.userId
import com.podcastapp.services.AuthService
import com.podcastapp.services.LibraryService
import com.podcastapp.services.RssImportService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

/** Усі маршрути цього файлу вимагають JWT (див. authenticate у Routing.kt). */
fun Route.meRoutes(auth: AuthService, library: LibraryService, rss: RssImportService) {
    get("/users/me") { call.respond(auth.profile(call.userId())) }

    post("/podcasts/import") {
        val r = call.receive<ImportRequest>()
        call.respond(HttpStatusCode.OK, rss.import(r.feedUrl))
    }

    post("/podcasts/{id}/subscribe") {
        library.subscribe(call.userId(), call.uuidParam("id"))
        call.respond(HttpStatusCode.NoContent)
    }
    delete("/podcasts/{id}/subscribe") {
        library.unsubscribe(call.userId(), call.uuidParam("id"))
        call.respond(HttpStatusCode.NoContent)
    }
    get("/me/subscriptions") {
        call.respond(library.subscriptions(call.userId(), call.pageParam(), call.sizeParam()))
    }

    post("/episodes/{id}/favorite") {
        library.favorite(call.userId(), call.uuidParam("id"))
        call.respond(HttpStatusCode.NoContent)
    }
    delete("/episodes/{id}/favorite") {
        library.unfavorite(call.userId(), call.uuidParam("id"))
        call.respond(HttpStatusCode.NoContent)
    }
    get("/me/favorites") {
        call.respond(library.favorites(call.userId(), call.pageParam(), call.sizeParam()))
    }

    put("/episodes/{id}/progress") {
        val r = call.receive<ProgressRequest>()
        library.saveProgress(call.userId(), call.uuidParam("id"), r.positionSec, r.completed)
        call.respond(HttpStatusCode.NoContent)
    }
    get("/me/history") {
        call.respond(library.history(call.userId(), call.pageParam(), call.sizeParam()))
    }
}
