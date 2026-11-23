package com.podcastapp

import com.podcastapp.config.AppConfig
import com.podcastapp.db.DatabaseFactory
import com.podcastapp.plugins.configurePlugins
import com.podcastapp.repositories.LibraryRepository
import com.podcastapp.repositories.PodcastRepository
import com.podcastapp.repositories.UserRepository
import com.podcastapp.routes.configureRouting
import com.podcastapp.security.JwtService
import com.podcastapp.services.AuthService
import com.podcastapp.services.CatalogService
import com.podcastapp.services.LibraryService
import com.podcastapp.services.RssImportService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.server.application.Application

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val cfg = AppConfig.from(environment.config)
    DatabaseFactory.init(cfg.db)

    val jwt = JwtService(cfg.jwt)
    configurePlugins(jwt)

    val podcastRepo = PodcastRepository()
    val catalog = CatalogService(podcastRepo)
    val auth = AuthService(UserRepository(), jwt)
    val library = LibraryService(LibraryRepository(), catalog)
    val http = HttpClient(CIO) {
        install(HttpTimeout) { requestTimeoutMillis = 10_000 }
        followRedirects = true
    }
    val rss = RssImportService(podcastRepo, http)

    configureRouting(auth, catalog, library, rss)
    if (cfg.rssRefreshEnabled) rss.startScheduler(this, cfg.rssIntervalMinutes)
}
