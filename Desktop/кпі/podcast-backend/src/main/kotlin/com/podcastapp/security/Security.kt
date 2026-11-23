package com.podcastapp.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.podcastapp.config.JwtConfig
import com.podcastapp.errors.UnauthorizedException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.UUID

object PasswordHasher {
    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(10))
    fun verify(password: String, hash: String): Boolean =
        runCatching { BCrypt.checkpw(password, hash) }.getOrDefault(false)
}

class JwtService(private val cfg: JwtConfig) {
    private val algorithm = Algorithm.HMAC256(cfg.secret)
    private val random = SecureRandom()

    val verifier: JWTVerifier = JWT.require(algorithm).withIssuer(cfg.issuer).build()
    val accessTtlSec: Long get() = cfg.accessTtlMinutes * 60
    val refreshTtlDays: Long get() = cfg.refreshTtlDays

    fun createAccessToken(userId: UUID): String = JWT.create()
        .withIssuer(cfg.issuer)
        .withSubject(userId.toString())
        .withExpiresAt(Date.from(Instant.now().plusSeconds(accessTtlSec)))
        .sign(algorithm)

    /** Випадковий непрозорий refresh-токен; у базі зберігається лише його SHA-256. */
    fun newRefreshToken(): String {
        val bytes = ByteArray(32).also { random.nextBytes(it) }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hashToken(token: String): String =
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
}

fun ApplicationCall.userId(): UUID {
    val sub = authentication.principal<JWTPrincipal>()?.payload?.subject
        ?: throw UnauthorizedException("Потрібна авторизація")
    return UUID.fromString(sub)
}
