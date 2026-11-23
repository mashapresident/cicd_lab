package com.podcastapp.repositories

import com.podcastapp.db.Categories
import com.podcastapp.db.Episodes
import com.podcastapp.db.Favorites
import com.podcastapp.db.ListeningProgress
import com.podcastapp.db.Podcasts
import com.podcastapp.db.Subscriptions
import com.podcastapp.db.dbQuery
import com.podcastapp.dto.EpisodeDto
import com.podcastapp.dto.HistoryItemDto
import com.podcastapp.dto.PodcastDto
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class LibraryRepository {

    // ---- підписки ----
    suspend fun subscribe(user: UUID, podcast: UUID) = dbQuery {
        Subscriptions.insertIgnore {
            it[Subscriptions.userId] = user
            it[Subscriptions.podcastId] = podcast
        }
        Unit
    }

    suspend fun unsubscribe(user: UUID, podcast: UUID) = dbQuery {
        Subscriptions.deleteWhere { (Subscriptions.userId eq user) and (Subscriptions.podcastId eq podcast) }
        Unit
    }

    suspend fun subscriptions(user: UUID, page: Int, size: Int): Pair<List<PodcastDto>, Long> = dbQuery {
        val q = Subscriptions.join(Podcasts, JoinType.INNER, Subscriptions.podcastId, Podcasts.id)
            .join(Categories, JoinType.LEFT, Podcasts.categoryId, Categories.id)
            .selectAll().where { Subscriptions.userId eq user }
        val total = q.count()
        q.orderBy(Subscriptions.createdAt, SortOrder.DESC)
            .limit(size, ((page - 1) * size).toLong())
            .map { it.toPodcastDto() } to total
    }

    // ---- обране ----
    suspend fun favorite(user: UUID, episode: UUID) = dbQuery {
        Favorites.insertIgnore {
            it[Favorites.userId] = user
            it[Favorites.episodeId] = episode
        }
        Unit
    }

    suspend fun unfavorite(user: UUID, episode: UUID) = dbQuery {
        Favorites.deleteWhere { (Favorites.userId eq user) and (Favorites.episodeId eq episode) }
        Unit
    }

    suspend fun favorites(user: UUID, page: Int, size: Int): Pair<List<EpisodeDto>, Long> = dbQuery {
        val q = Favorites.join(Episodes, JoinType.INNER, Favorites.episodeId, Episodes.id)
            .selectAll().where { Favorites.userId eq user }
        val total = q.count()
        q.orderBy(Favorites.createdAt, SortOrder.DESC)
            .limit(size, ((page - 1) * size).toLong())
            .map { it.toEpisodeDto() } to total
    }

    // ---- прогрес та історія ----
    suspend fun saveProgress(user: UUID, episode: UUID, positionSec: Int, completed: Boolean) = dbQuery {
        val now = Instant.now()
        val updated = ListeningProgress.update({
            (ListeningProgress.userId eq user) and (ListeningProgress.episodeId eq episode)
        }) {
            it[ListeningProgress.positionSec] = positionSec
            it[ListeningProgress.completed] = completed
            it[ListeningProgress.updatedAt] = now
        }
        if (updated == 0) {
            ListeningProgress.insert {
                it[ListeningProgress.userId] = user
                it[ListeningProgress.episodeId] = episode
                it[ListeningProgress.positionSec] = positionSec
                it[ListeningProgress.completed] = completed
                it[ListeningProgress.updatedAt] = now
            }
        }
        Unit
    }

    suspend fun history(user: UUID, page: Int, size: Int): Pair<List<HistoryItemDto>, Long> = dbQuery {
        val q = ListeningProgress.join(Episodes, JoinType.INNER, ListeningProgress.episodeId, Episodes.id)
            .selectAll().where { ListeningProgress.userId eq user }
        val total = q.count()
        q.orderBy(ListeningProgress.updatedAt, SortOrder.DESC)
            .limit(size, ((page - 1) * size).toLong())
            .map {
                HistoryItemDto(
                    episode = it.toEpisodeDto(),
                    positionSec = it[ListeningProgress.positionSec],
                    completed = it[ListeningProgress.completed],
                    updatedAt = it[ListeningProgress.updatedAt].toString(),
                )
            } to total
    }
}
