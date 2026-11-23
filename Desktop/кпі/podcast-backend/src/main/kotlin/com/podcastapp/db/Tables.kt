package com.podcastapp.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Users : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val displayName = varchar("display_name", 100)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

object RefreshTokens : UUIDTable("refresh_tokens") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val tokenHash = varchar("token_hash", 64).uniqueIndex()
    val expiresAt = timestamp("expires_at")
}

object Categories : IntIdTable("categories") {
    val name = varchar("name", 100).uniqueIndex()
}

object Podcasts : UUIDTable("podcasts") {
    val title = varchar("title", 500)
    val author = varchar("author", 300).default("")
    val description = text("description").nullable()
    val coverUrl = text("cover_url").nullable()
    val feedUrl = varchar("feed_url", 1000).uniqueIndex()
    val language = varchar("language", 20).nullable()
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.SET_NULL).nullable()
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

object Episodes : UUIDTable("episodes") {
    val podcastId = reference("podcast_id", Podcasts, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 500)
    val description = text("description").nullable()
    val audioUrl = text("audio_url")
    val durationSec = integer("duration_sec").nullable()
    val publishedAt = timestamp("published_at")
    val guid = varchar("guid", 500)

    init { uniqueIndex(podcastId, guid) }
}

object Subscriptions : Table("subscriptions") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val podcastId = reference("podcast_id", Podcasts, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    override val primaryKey = PrimaryKey(userId, podcastId)
}

object Favorites : Table("favorites") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val episodeId = reference("episode_id", Episodes, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    override val primaryKey = PrimaryKey(userId, episodeId)
}

object ListeningProgress : Table("listening_progress") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val episodeId = reference("episode_id", Episodes, onDelete = ReferenceOption.CASCADE)
    val positionSec = integer("position_sec")
    val completed = bool("completed").default(false)
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
    override val primaryKey = PrimaryKey(userId, episodeId)
}
