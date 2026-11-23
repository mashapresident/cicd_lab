package com.podcastapp.dto

import kotlinx.serialization.Serializable

@Serializable data class RegisterRequest(val email: String, val password: String, val displayName: String)
@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class RefreshRequest(val refreshToken: String)
@Serializable data class TokensDto(val accessToken: String, val refreshToken: String, val expiresInSec: Long)
@Serializable data class UserDto(val id: String, val email: String, val displayName: String)

@Serializable data class CategoryDto(val id: Int, val name: String)

@Serializable
data class PodcastDto(
    val id: String,
    val title: String,
    val author: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val feedUrl: String,
    val language: String? = null,
    val category: String? = null,
)

@Serializable
data class EpisodeDto(
    val id: String,
    val podcastId: String,
    val title: String,
    val description: String? = null,
    val audioUrl: String,
    val durationSec: Int? = null,
    val publishedAt: String,
)

@Serializable data class PageDto<T>(val items: List<T>, val page: Int, val size: Int, val total: Long)

@Serializable data class ProgressRequest(val positionSec: Int, val completed: Boolean = false)

@Serializable
data class HistoryItemDto(
    val episode: EpisodeDto,
    val positionSec: Int,
    val completed: Boolean,
    val updatedAt: String,
)

@Serializable data class ImportRequest(val feedUrl: String)
@Serializable data class ImportResultDto(val podcast: PodcastDto, val newEpisodes: Int)

@Serializable data class ErrorDto(val code: String, val message: String)
@Serializable data class HealthDto(val status: String)
