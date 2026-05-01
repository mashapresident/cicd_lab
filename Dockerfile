# Multi-stage Dockerfile for GitHub Actions CI/CD Demo
FROM python:3.11-slim AS base

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better layer caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Development stage
FROM base AS development

COPY . .

RUN pip install --no-cache-dir pytest pytest-cov flake8 black

CMD ["pytest", "tests/", "-v"]

# Production stage
FROM base AS production

COPY src/ ./src/

# Create non-root user for security
RUN useradd --create-home --shell /bin/bash appuser && \
    chown -R appuser:appuser /app

USER appuser

CMD ["python", "-c", "from src.calculator import fibonacci; print('Calculator ready:', fibonacci(10))"]

# Test runner stage
FROM development AS test-runner

CMD ["pytest", "tests/", "--tb=short", "-v", "--color=yes"]
