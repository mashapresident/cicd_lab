package com.podcastapp.repositories

import com.podcastapp.db.RefreshTokens
import com.podcastapp.db.Users
import com.podcastapp.db.dbQuery
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

data class UserRow(val id: UUID, val email: String, val passwordHash: String, val displayName: String)

class UserRepository {
    private fun org.jetbrains.exposed.sql.ResultRow.toUser() =
        UserRow(this[Users.id].value, this[Users.email], this[Users.passwordHash], this[Users.displayName])

    suspend fun create(email: String, passwordHash: String, displayName: String): UserRow = dbQuery {
        val id = UUID.randomUUID()
        Users.insert {
            it[Users.id] = id
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.displayName] = displayName
        }
        UserRow(id, email, passwordHash, displayName)
    }

    suspend fun findByEmail(email: String): UserRow? = dbQuery {
        Users.selectAll().where { Users.email eq email }.singleOrNull()?.toUser()
    }

    suspend fun findById(id: UUID): UserRow? = dbQuery {
        Users.selectAll().where { Users.id eq id }.singleOrNull()?.toUser()
    }

    suspend fun saveRefreshToken(userId: UUID, tokenHash: String, expiresAt: Instant) = dbQuery {
        RefreshTokens.insert {
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.tokenHash] = tokenHash
            it[RefreshTokens.expiresAt] = expiresAt
        }
        Unit
    }

    /** Повертає (userId, expiresAt) і одразу видаляє токен (ротація). */
    suspend fun consumeRefreshToken(tokenHash: String): Pair<UUID, Instant>? = dbQuery {
        val row = RefreshTokens.selectAll().where { RefreshTokens.tokenHash eq tokenHash }.singleOrNull()
        if (row != null) RefreshTokens.deleteWhere { RefreshTokens.tokenHash eq tokenHash }
        row?.let { it[RefreshTokens.userId].value to it[RefreshTokens.expiresAt] }
    }
}
