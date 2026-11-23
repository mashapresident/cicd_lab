package com.podcastapp.routes

import com.podcastapp.dto.LoginRequest
import com.podcastapp.dto.RefreshRequest
import com.podcastapp.dto.RegisterRequest
import com.podcastapp.plugins.AUTH_RATE_LIMIT
import com.podcastapp.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(auth: AuthService) {
    rateLimit(AUTH_RATE_LIMIT) {
        route("/auth") {
            post("/register") {
                val r = call.receive<RegisterRequest>()
                call.respond(HttpStatusCode.Created, auth.register(r.email, r.password, r.displayName))
            }
            post("/login") {
                val r = call.receive<LoginRequest>()
                call.respond(auth.login(r.email, r.password))
            }
            post("/refresh") {
                val r = call.receive<RefreshRequest>()
                call.respond(auth.refresh(r.refreshToken))
            }
        }
    }
}
