package com.podcastapp.services

import com.podcastapp.dto.TokensDto
import com.podcastapp.dto.UserDto
import com.podcastapp.errors.ConflictException
import com.podcastapp.errors.NotFoundException
import com.podcastapp.errors.UnauthorizedException
import com.podcastapp.errors.ValidationException
import com.podcastapp.repositories.UserRepository
import com.podcastapp.security.JwtService
import com.podcastapp.security.PasswordHasher
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AuthService(private val users: UserRepository, private val jwt: JwtService) {
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    suspend fun register(email: String, password: String, displayName: String): TokensDto {
        val norm = email.trim().lowercase()
        if (!emailRegex.matches(norm)) throw ValidationException("Некоректна електронна пошта")
        if (password.length < 8) throw ValidationException("Пароль має містити щонайменше 8 символів")
        if (displayName.isBlank() || displayName.length > 100) throw ValidationException("Некоректне ім'я")
        if (users.findByEmail(norm) != null) throw emailTaken()

        val user = try {
            users.create(norm, PasswordHasher.hash(password), displayName.trim())
        } catch (e: ExposedSQLException) {
            throw emailTaken() // гонка двох паралельних реєстрацій
        }
        return issueTokens(user.id)
    }

    suspend fun login(email: String, password: String): TokensDto {
        val user = users.findByEmail(email.trim().lowercase())
        if (user == null || !PasswordHasher.verify(password, user.passwordHash)) {
            throw UnauthorizedException("Невірна пошта або пароль")
        }
        return issueTokens(user.id)
    }

    suspend fun refresh(refreshToken: String): TokensDto {
        val (userId, expiresAt) = users.consumeRefreshToken(jwt.hashToken(refreshToken))
            ?: throw UnauthorizedException("Сесія завершилась")
        if (expiresAt.isBefore(Instant.now())) throw UnauthorizedException("Сесія завершилась")
        return issueTokens(userId)
    }

    suspend fun profile(id: UUID): UserDto {
        val u = users.findById(id) ?: throw NotFoundException("Користувача не знайдено")
        return UserDto(u.id.toString(), u.email, u.displayName)
    }

    private suspend fun issueTokens(userId: UUID): TokensDto {
        val refresh = jwt.newRefreshToken()
        users.saveRefreshToken(
            userId, jwt.hashToken(refresh), Instant.now().plus(jwt.refreshTtlDays, ChronoUnit.DAYS)
        )
        return TokensDto(jwt.createAccessToken(userId), refresh, jwt.accessTtlSec)
    }

    private fun emailTaken() = ConflictException("EMAIL_TAKEN", "Користувач з такою поштою вже існує")
}
