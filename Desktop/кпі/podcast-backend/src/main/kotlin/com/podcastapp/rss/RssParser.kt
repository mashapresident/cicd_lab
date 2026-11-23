package com.podcastapp.rss

import com.podcastapp.errors.UnprocessableException
import com.rometools.modules.itunes.AbstractITunesObject
import com.rometools.modules.itunes.EntryInformation
import com.rometools.modules.itunes.FeedInformation
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.io.ByteArrayInputStream
import java.time.Instant

data class ParsedEpisode(
    val guid: String,
    val title: String,
    val description: String?,
    val audioUrl: String,
    val durationSec: Int?,
    val publishedAt: Instant,
)

data class ParsedFeed(
    val title: String,
    val author: String,
    val description: String?,
    val coverUrl: String?,
    val language: String?,
    val category: String?,
    val episodes: List<ParsedEpisode>,
)

object RssParser {
    fun parse(bytes: ByteArray): ParsedFeed {
        val feed = try {
            // Rome за замовчуванням забороняє DOCTYPE, тому XXE неможливий
            SyndFeedInput().build(XmlReader(ByteArrayInputStream(bytes)))
        } catch (e: Exception) {
            throw UnprocessableException("Не вдалося розібрати RSS-стрічку")
        }
        val info = feed.getModule(AbstractITunesObject.URI) as? FeedInformation

        val episodes = feed.entries.mapNotNull { e ->
            val audio = e.enclosures.firstOrNull { it.type?.startsWith("audio") == true }?.url
                ?: return@mapNotNull null
            val ei = e.getModule(AbstractITunesObject.URI) as? EntryInformation
            ParsedEpisode(
                guid = (e.uri?.takeIf { it.isNotBlank() } ?: audio).take(500),
                title = (e.title ?: "Без назви").take(500),
                description = e.description?.value,
                audioUrl = audio,
                durationSec = ei?.duration?.milliseconds?.div(1000)?.toInt(),
                publishedAt = (e.publishedDate ?: e.updatedDate)?.toInstant() ?: Instant.now(),
            )
        }

        return ParsedFeed(
            title = (feed.title ?: "Без назви").take(500),
            author = (info?.author ?: feed.author ?: "").take(300),
            description = feed.description,
            coverUrl = feed.image?.url,
            language = feed.language?.take(20),
            category = info?.categories?.firstOrNull()?.name?.take(100),
            episodes = episodes,
        )
    }
}
