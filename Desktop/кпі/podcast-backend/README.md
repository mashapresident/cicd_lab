# Podcast App — Backend

Серверна частина додатка для подкастів (Kotlin, Ktor, PostgreSQL).

## Запуск
```bash
cp .env.example .env        # задайте DB_PASSWORD і JWT_SECRET
docker compose up --build
curl http://localhost:8080/health
```
Для Android-емулятора адреса сервера: `http://10.0.2.2:8080/api/v1/`.

## Без Docker
Потрібні JDK 17 і PostgreSQL 15+ (база `podcasts`, користувач `podcasts`).
```bash
./gradlew run
```

## Тести
```bash
./gradlew test     # ApiFlowTest потребує запущеного Docker (Testcontainers)
```

## Gradle Wrapper
Якщо в репозиторії немає `gradlew`, один раз виконайте `gradle wrapper --gradle-version 8.8`
(або створіть порожній Ktor-проєкт у IntelliJ IDEA і скопіюйте `gradle/` та `gradlew*`).

## API
Контракт — у `openapi.yaml` (відкрийте у Swagger Editor).
