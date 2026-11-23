package com.podcastapp.config

import io.ktor.server.config.ApplicationConfig

data class DbConfig(val url: String, val user: String, val password: String)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val accessTtlMinutes: Long,
    val refreshTtlDays: Long,
)

data class AppConfig(
    val db: DbConfig,
    val jwt: JwtConfig,
    val rssRefreshEnabled: Boolean,
    val rssIntervalMinutes: Long,
) {
    companion object {
        fun from(c: ApplicationConfig) = AppConfig(
            db = DbConfig(
                url = c.property("app.db.url").getString(),
                user = c.property("app.db.user").getString(),
                password = c.property("app.db.password").getString(),
            ),
            jwt = JwtConfig(
                secret = c.property("app.jwt.secret").getString(),
                issuer = c.property("app.jwt.issuer").getString(),
                accessTtlMinutes = c.property("app.jwt.accessTtlMinutes").getString().toLong(),
                refreshTtlDays = c.property("app.jwt.refreshTtlDays").getString().toLong(),
            ),
            rssRefreshEnabled = c.property("app.rss.enabled").getString().toBoolean(),
            rssIntervalMinutes = c.property("app.rss.refreshIntervalMinutes").getString().toLong(),
        )
    }
}
