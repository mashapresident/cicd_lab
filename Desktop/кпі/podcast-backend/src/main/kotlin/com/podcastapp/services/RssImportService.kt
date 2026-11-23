package com.podcastapp.services

import com.podcastapp.dto.ImportResultDto
import com.podcastapp.errors.ApiException
import com.podcastapp.errors.UnprocessableException
import com.podcastapp.repositories.PodcastRepository
import com.podcastapp.rss.RssParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.URI

class RssImportService(private val repo: PodcastRepository, private val client: HttpClient) {
    private val log = LoggerFactory.getLogger(RssImportService::class.java)
    private val maxBytes = 10 * 1024 * 1024

    suspend fun import(feedUrl: String): ImportResultDto {
        val url = validate(feedUrl.trim())
        val bytes = try {
            val response: HttpResponse = client.get(url)
            if (!response.status.isSuccess()) throw UnprocessableException("RSS-стрічка недоступна")
            response.body<ByteArray>()
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw UnprocessableException("RSS-стрічка недоступна")
        }
        if (bytes.size > maxBytes) throw UnprocessableException("RSS-стрічка завелика")
        return repo.saveFeed(url, RssParser.parse(bytes))
    }

    /** Захист від SSRF: лише http/https і лише публічні адреси. */
    private suspend fun validate(feedUrl: String): String {
        val uri = try { URI(feedUrl) } catch (e: Exception) { throw UnprocessableException("Некоректне посилання") }
        if (uri.scheme !in listOf("http", "https") || uri.host.isNullOrBlank()) {
            throw UnprocessableException("Дозволені лише http/https-посилання")
        }
        val addr = try {
            withContext(Dispatchers.IO) { InetAddress.getByName(uri.host) }
        } catch (e: Exception) {
            throw UnprocessableException("Хост не знайдено")
        }
        if (addr.isLoopbackAddress || addr.isSiteLocalAddress || addr.isLinkLocalAddress || addr.isAnyLocalAddress) {
            throw UnprocessableException("Адреса недопустима")
        }
        return feedUrl
    }

    fun startScheduler(scope: CoroutineScope, everyMinutes: Long) {
        scope.launch {
            while (isActive) {
                delay(everyMinutes * 60_000)
                for (url in repo.allFeedUrls()) {
                    try { import(url) } catch (e: Exception) { log.warn("Не вдалося оновити {}: {}", url, e.message) }
                }
            }
        }
    }
}
