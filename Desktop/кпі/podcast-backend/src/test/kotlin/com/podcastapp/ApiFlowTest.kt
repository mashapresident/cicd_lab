package com.podcastapp

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Інтеграційні тести: потрібен запущений Docker (Testcontainers підніме PostgreSQL). */
class ApiFlowTest {
    companion object {
        private val pg = PostgreSQLContainer("postgres:16-alpine").apply { start() }
    }

    private fun ApplicationTestBuilder.setup() {
        environment {
            config = MapApplicationConfig(
                "app.db.url" to pg.jdbcUrl,
                "app.db.user" to pg.username,
                "app.db.password" to pg.password,
                "app.jwt.secret" to "test-secret-test-secret-test-secret",
                "app.jwt.issuer" to "test",
                "app.jwt.accessTtlMinutes" to "15",
                "app.jwt.refreshTtlDays" to "30",
                "app.rss.enabled" to "false",
                "app.rss.refreshIntervalMinutes" to "60",
            )
        }
        application { module() }
    }

    private suspend fun ApplicationTestBuilder.register(email: String) = client.post("/api/v1/auth/register") {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody("""{"email":"$email","password":"Passw0rd!","displayName":"Test"}""")
    }

    @Test fun registerThenDuplicateIsConflict() = testApplication {
        setup()
        assertEquals(HttpStatusCode.Created, register("a@example.com").status)
        val dup = register("a@example.com")
        assertEquals(HttpStatusCode.Conflict, dup.status)
        assertTrue(dup.bodyAsText().contains("EMAIL_TAKEN"))
    }

    @Test fun wrongPasswordIsUnauthorized() = testApplication {
        setup()
        register("b@example.com")
        val r = client.post("/api/v1/auth/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"email":"b@example.com","password":"wrong-password"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, r.status)
    }

    @Test fun privateRouteNeedsTokenAndWorksWithOne() = testApplication {
        setup()
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/me/subscriptions").status)

        val body = register("c@example.com").bodyAsText()
        val token = Json.parseToJsonElement(body).jsonObject["accessToken"]!!.jsonPrimitive.content
        val ok = client.get("/api/v1/me/subscriptions") { header(HttpHeaders.Authorization, "Bearer $token") }
        assertEquals(HttpStatusCode.OK, ok.status)
    }

    @Test fun publicCatalogReturnsPagedCategories() = testApplication {
        setup()
        val r = client.get("/api/v1/podcasts?query=nothing&page=1&size=10")
        assertEquals(HttpStatusCode.OK, r.status)
        assertTrue(r.bodyAsText().contains("\"total\":0"))
        assertEquals(HttpStatusCode.OK, client.get("/api/v1/categories").status)
        assertEquals(HttpStatusCode.OK, client.get("/health").status)
    }
}
