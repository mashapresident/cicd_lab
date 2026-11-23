package com.podcastapp

import com.podcastapp.config.JwtConfig
import com.podcastapp.security.JwtService
import com.podcastapp.security.PasswordHasher
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SecurityTest {
    private val jwt = JwtService(JwtConfig("test-secret-test-secret-test-secret", "test", 15, 30))

    @Test fun passwordHashVerifies() {
        val h = PasswordHasher.hash("Passw0rd!")
        assertNotEquals("Passw0rd!", h)
        assertTrue(PasswordHasher.verify("Passw0rd!", h))
        assertFalse(PasswordHasher.verify("wrong", h))
    }

    @Test fun accessTokenContainsUserId() {
        val id = UUID.randomUUID()
        val decoded = jwt.verifier.verify(jwt.createAccessToken(id))
        assertEquals(id.toString(), decoded.subject)
    }

    @Test fun refreshTokenHashIsStableAndDifferentFromToken() {
        val t = jwt.newRefreshToken()
        assertEquals(jwt.hashToken(t), jwt.hashToken(t))
        assertNotEquals(t, jwt.hashToken(t))
    }
}
