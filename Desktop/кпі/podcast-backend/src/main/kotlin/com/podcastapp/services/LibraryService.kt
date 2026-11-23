package com.podcastapp.services

import com.podcastapp.dto.EpisodeDto
import com.podcastapp.dto.HistoryItemDto
import com.podcastapp.dto.PageDto
import com.podcastapp.dto.PodcastDto
import com.podcastapp.errors.ValidationException
import com.podcastapp.repositories.LibraryRepository
import java.util.UUID

class LibraryService(private val library: LibraryRepository, private val catalog: CatalogService) {

    suspend fun subscribe(user: UUID, podcast: UUID) {
        catalog.podcast(podcast)
        library.subscribe(user, podcast)
    }

    suspend fun unsubscribe(user: UUID, podcast: UUID) = library.unsubscribe(user, podcast)

    suspend fun subscriptions(user: UUID, page: Int, size: Int): PageDto<PodcastDto> {
        val (items, total) = library.subscriptions(user, page, size)
        return PageDto(items, page, size, total)
    }

    suspend fun favorite(user: UUID, episode: UUID) {
        catalog.episode(episode)
        library.favorite(user, episode)
    }

    suspend fun unfavorite(user: UUID, episode: UUID) = library.unfavorite(user, episode)

    suspend fun favorites(user: UUID, page: Int, size: Int): PageDto<EpisodeDto> {
        val (items, total) = library.favorites(user, page, size)
        return PageDto(items, page, size, total)
    }

    suspend fun saveProgress(user: UUID, episode: UUID, positionSec: Int, completed: Boolean) {
        if (positionSec < 0) throw ValidationException("Позиція не може бути від'ємною")
        catalog.episode(episode)
        library.saveProgress(user, episode, positionSec, completed)
    }

    suspend fun history(user: UUID, page: Int, size: Int): PageDto<HistoryItemDto> {
        val (items, total) = library.history(user, page, size)
        return PageDto(items, page, size, total)
    }
}
