package com.podcastapp.services

import com.podcastapp.dto.CategoryDto
import com.podcastapp.dto.EpisodeDto
import com.podcastapp.dto.PageDto
import com.podcastapp.dto.PodcastDto
import com.podcastapp.errors.NotFoundException
import com.podcastapp.repositories.PodcastRepository
import java.util.UUID

class CatalogService(private val repo: PodcastRepository) {
    suspend fun categories(): List<CategoryDto> = repo.categories()

    suspend fun search(q: String?, category: Int?, page: Int, size: Int): PageDto<PodcastDto> {
        val (items, total) = repo.search(q, category, page, size)
        return PageDto(items, page, size, total)
    }

    suspend fun podcast(id: UUID): PodcastDto = repo.findById(id) ?: throw NotFoundException("Подкаст не знайдено")

    suspend fun episodes(podcastId: UUID, page: Int, size: Int): PageDto<EpisodeDto> {
        podcast(podcastId)
        val (items, total) = repo.episodes(podcastId, page, size)
        return PageDto(items, page, size, total)
    }

    suspend fun episode(id: UUID): EpisodeDto = repo.episodeById(id) ?: throw NotFoundException("Епізод не знайдено")
}
