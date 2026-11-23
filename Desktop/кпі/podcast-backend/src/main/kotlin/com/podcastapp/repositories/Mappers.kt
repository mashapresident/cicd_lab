package com.podcastapp.repositories

import com.podcastapp.db.Categories
import com.podcastapp.db.Episodes
import com.podcastapp.db.Podcasts
import com.podcastapp.dto.EpisodeDto
import com.podcastapp.dto.PodcastDto
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toPodcastDto() = PodcastDto(
    id = this[Podcasts.id].value.toString(),
    title = this[Podcasts.title],
    author = this[Podcasts.author],
    description = this[Podcasts.description],
    coverUrl = this[Podcasts.coverUrl],
    feedUrl = this[Podcasts.feedUrl],
    language = this[Podcasts.language],
    category = this.getOrNull(Categories.name),
)

fun ResultRow.toEpisodeDto() = EpisodeDto(
    id = this[Episodes.id].value.toString(),
    podcastId = this[Episodes.podcastId].value.toString(),
    title = this[Episodes.title],
    description = this[Episodes.description],
    audioUrl = this[Episodes.audioUrl],
    durationSec = this[Episodes.durationSec],
    publishedAt = this[Episodes.publishedAt].toString(),
)
