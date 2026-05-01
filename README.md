# GitHub Actions CI/CD Demo

Демонстраційний проект для вивчення **GitHub Actions** — автоматизація тестування, лінтингу, збірки Docker та генерації звітів.

---

## 📁 Структура проекту

```
├── src/
│   ├── __init__.py
│   ├── calculator.py      # Математичні функції
│   └── string_utils.py    # Рядкові утиліти
├── tests/
│   ├── __init__.py
│   ├── test_calculator.py
│   └── test_string_utils.py
├── reports/
│   └── generate_report.py # Генератор HTML-звіту
├── .github/
│   └── workflows/
│       ├── 1-pytest.yml       # Задача 1: Pytest
│       ├── 2-linting.yml      # Задача 2: Linting
│       ├── 3-multiversion.yml # Задача 3: Multi-version
│       ├── 4-docker.yml       # Задача 4: Docker
│       └── 5-html-report.yml  # Задача 5: HTML Report
├── Dockerfile
├── requirements.txt
└── README.md
```

---

## GitHub Actions Workflows

### 1. Автоматичне тестування з Pytest
**Файл:** `.github/workflows/1-pytest.yml`

Автоматично запускає всі тести з `pytest` при кожному `push` або `pull_request`.

- Встановлює Python 3.11
- Встановлює залежності з `requirements.txt`
- Запускає `pytest tests/` з детальним виводом

---

### 2. Перевірка стилю коду (Linting)
**Файл:** `.github/workflows/2-linting.yml`

Перевіряє якість і стиль коду за допомогою кількох інструментів:

| Інструмент | Призначення |
|-----------|-------------|
| `flake8`  | PEP8 style check |
| `black`   | Code formatting |
| `isort`   | Import sorting |
| `pylint`  | Static analysis |

---

### 3. Мультиверсійне тестування
**Файл:** `.github/workflows/3-multiversion.yml`

Запускає тести на **матриці** Python версій та операційних систем:

| Python | Ubuntu | Windows | macOS |
|--------|--------|---------|-------|
| 3.9    | ✅     | —       | —     |
| 3.10   | ✅     | ✅      | ✅    |
| 3.11   | ✅     | ✅      | ✅    |
| 3.12   | ✅     | ✅      | ✅    |

---

### 4. Збірка Docker-контейнера
**Файл:** `.github/workflows/4-docker.yml`

Автоматично будує та тестує Docker-образ:

- Multi-stage Dockerfile (dev + production)
- Кешування шарів через GitHub Actions Cache
- Публікація образу до **GitHub Container Registry** (`ghcr.io`)
- Запуск тестів усередині контейнера

```bash
docker pull ghcr.io/<your-username>/<repo-name>:main
```

---

### 5. Автоматичний HTML-звіт
**Файл:** `.github/workflows/5-html-report.yml`

Генерує красивий HTML-звіт з результатами тестів та покриттям коду:

- Збирає результати тестів у JSON
- Генерує coverage report
- Публікує звіт на **GitHub Pages**
- Зберігає як artifact на 30 днів

---

## Локальний запуск

```bash
# Встановити залежності
pip install -r requirements.txt

# Запустити тести
pytest tests/ -v

# Перевірити стиль коду
flake8 src/ tests/
black --check src/ tests/
isort --check-only src/ tests/

# Зібрати Docker
docker build --target development -t myapp:dev .
docker run --rm myapp:dev
```
