package com.podcastapp.repositories

import com.podcastapp.db.Categories
import com.podcastapp.db.Episodes
import com.podcastapp.db.Podcasts
import com.podcastapp.db.dbQuery
import com.podcastapp.dto.CategoryDto
import com.podcastapp.dto.EpisodeDto
import com.podcastapp.dto.ImportResultDto
import com.podcastapp.dto.PodcastDto
import com.podcastapp.rss.ParsedFeed
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

class PodcastRepository {
    private val podcastJoin =
        Podcasts.join(Categories, JoinType.LEFT, onColumn = Podcasts.categoryId, otherColumn = Categories.id)

    suspend fun categories(): List<CategoryDto> = dbQuery {
        Categories.selectAll().orderBy(Categories.name)
            .map { CategoryDto(it[Categories.id].value, it[Categories.name]) }
    }

    suspend fun search(q: String?, categoryId: Int?, page: Int, size: Int): Pair<List<PodcastDto>, Long> = dbQuery {
        var query = podcastJoin.selectAll()
        if (!q.isNullOrBlank()) {
            val escaped = q.trim().lowercase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
            val pattern = "%$escaped%"
            query = query.andWhere {
                (Podcasts.title.lowerCase() like pattern) or (Podcasts.author.lowerCase() like pattern)
            }
        }
        if (categoryId != null) {
            query = query.andWhere { Podcasts.categoryId eq EntityID(categoryId, Categories) }
        }
        val total = query.count()
        val items = query.orderBy(Podcasts.title)
            .limit(size, ((page - 1) * size).toLong())
            .map { it.toPodcastDto() }
        items to total
    }

    private fun byId(id: UUID): PodcastDto? =
        podcastJoin.selectAll().where { Podcasts.id eq id }.singleOrNull()?.toPodcastDto()

    suspend fun findById(id: UUID): PodcastDto? = dbQuery { byId(id) }

    suspend fun episodes(podcastId: UUID, page: Int, size: Int): Pair<List<EpisodeDto>, Long> = dbQuery {
        val q = Episodes.selectAll().where { Episodes.podcastId eq podcastId }
        val total = q.count()
        val items = q.orderBy(Episodes.publishedAt, SortOrder.DESC)
            .limit(size, ((page - 1) * size).toLong())
            .map { it.toEpisodeDto() }
        items to total
    }

    suspend fun episodeById(id: UUID): EpisodeDto? = dbQuery {
        Episodes.selectAll().where { Episodes.id eq id }.singleOrNull()?.toEpisodeDto()
    }

    suspend fun allFeedUrls(): List<String> = dbQuery {
        Podcasts.select(Podcasts.feedUrl).map { it[Podcasts.feedUrl] }
    }

    /** Створює або оновлює подкаст і додає лише нові епізоди (унікальність за podcast_id + guid). */
    suspend fun saveFeed(feedUrl: String, f: ParsedFeed): ImportResultDto = dbQuery {
        val catId: Int? = f.category?.let { catName ->
            Categories.selectAll().where { Categories.name eq catName }.singleOrNull()?.get(Categories.id)?.value
                ?: Categories.insertAndGetId { it[Categories.name] = catName }.value
        }
        val now = Instant.now()
        val existing = Podcasts.selectAll().where { Podcasts.feedUrl eq feedUrl }.singleOrNull()

        val podcastId: UUID = if (existing == null) {
            Podcasts.insertAndGetId {
                it[Podcasts.title] = f.title
                it[Podcasts.author] = f.author
                it[Podcasts.description] = f.description
                it[Podcasts.coverUrl] = f.coverUrl
                it[Podcasts.feedUrl] = feedUrl
                it[Podcasts.language] = f.language
                it[Podcasts.categoryId] = catId?.let { c -> EntityID(c, Categories) }
                it[Podcasts.updatedAt] = now
            }.value
        } else {
            val id = existing[Podcasts.id].value
            Podcasts.update({ Podcasts.id eq id }) {
                it[Podcasts.title] = f.title
                it[Podcasts.author] = f.author
                it[Podcasts.description] = f.description
                it[Podcasts.coverUrl] = f.coverUrl
                it[Podcasts.language] = f.language
                it[Podcasts.categoryId] = catId?.let { c -> EntityID(c, Categories) }
                it[Podcasts.updatedAt] = now
            }
            id
        }

        val known = Episodes.select(Episodes.guid).where { Episodes.podcastId eq podcastId }
            .map { it[Episodes.guid] }.toHashSet()
        val fresh = f.episodes.distinctBy { it.guid }.filter { it.guid !in known }
        Episodes.batchInsert(fresh) { e ->
            this[Episodes.podcastId] = podcastId
            this[Episodes.title] = e.title
            this[Episodes.description] = e.description
            this[Episodes.audioUrl] = e.audioUrl
            this[Episodes.durationSec] = e.durationSec
            this[Episodes.publishedAt] = e.publishedAt
            this[Episodes.guid] = e.guid
        }
        ImportResultDto(podcast = byId(podcastId)!!, newEpisodes = fresh.size)
    }
}
