# API Testing Framework (Python)

**Stack:** Python 3.10+ · Requests · PyTest · pytest-xdist (parallel execution) · Allure Reports · Faker

A REST API testing framework demonstrating skills that matter most to recruiters: authentication flows, positive/negative coverage, response-time validation, parallel execution, and structured reporting — built entirely in Python.

---

## Project Structure

```
api-testing-framework-python/
├── requirements.txt
├── pytest.ini
├── config/
│   └── config.ini              # base URL, tokens, environment
├── src/
│   ├── config/
│   │   └── config_manager.py   # singleton config loader (env-var override)
│   ├── models/
│   │   ├── user.py             # User / Address / Company dataclasses
│   │   └── auth_models.py      # LoginRequest / RefreshRequest dataclasses
│   └── utils/
│       ├── api_utils.py        # assertion helpers
│       └── test_data_factory.py# Faker-powered test data
└── tests/
    ├── conftest.py              # session fixtures + result logging
    ├── test_get_api.py          # 10 GET test cases
    ├── test_post_api.py         # 9 POST test cases
    ├── test_put_delete_api.py   # 11 PUT/DELETE test cases
    └── test_auth_api.py         # 13 Auth test cases
```

---

## Test Case Coverage (43 cases total)

| Area | Smoke | Regression | Auth | Negative | Performance |
|---|---|---|---|---|---|
| GET | 2 | 8 | 1 | 2 | 1 |
| POST | 1 | 8 | 2 | 4 | 1 |
| PUT | 1 | 5 | 1 | 2 | 1 |
| DELETE | 1 | 4 | 2 | 1 | 1 |
| Auth | 2 | 13 | 9 | 5 | 1 |

### Scenario types covered
- ✅ Valid request (happy path)
- ✅ Invalid request (malformed data, wrong types)
- ✅ Missing mandatory fields (name, email, password, username)
- ✅ Unauthorized access (no token, invalid token, wrong scheme)
- ✅ Insufficient permissions (user role attempting admin action)
- ✅ Response time validation (per-endpoint SLA assertions)
- ✅ Resource not found (non-existent IDs)
- ✅ Token lifecycle (obtain → use → refresh → logout)

---

## Quick Start

### 1. Prerequisites
- Python 3.10+
- pip

### 2. Install dependencies
```bash
pip install -r requirements.txt
```

### 3. Configure
Edit `config/config.ini`:
```ini
[DEFAULT]
base_url = https://your-api.example.com
auth_admin_token = <your_admin_jwt>
auth_user_token = <your_user_jwt>
```
Or override any key at runtime via environment variables (upper-case):
```bash
BASE_URL=https://staging.example.com ENVIRONMENT=staging pytest
```

### 4. Run tests

```bash
# Full suite
pytest

# Smoke tests only
pytest -m smoke

# Run in parallel (4 workers)
pytest -n 4

# Single test file
pytest tests/test_auth_api.py

# Generate Allure results, then view report
pytest --alluredir=allure-results
allure serve allure-results
```

---

## Key Framework Features

### Shared Session Fixtures (`conftest.py`)
Mirrors Rest Assured's `RequestSpecification` pattern using `requests.Session`:
- `auth_admin_spec` — Bearer token with admin privileges
- `auth_user_spec` — Bearer token with user privileges
- `no_auth_spec` — No Authorization header (for 401 tests)
- `request_spec` — Base session (content-type headers only)

### Allure Reporting
Every test is annotated with `@allure.epic`, `@allure.feature`, `@allure.story`, `@allure.description`, and `@allure.severity`, matching the original Java framework's reporting structure.

### Parallel Execution
`pytest-xdist` runs tests across multiple workers (`pytest -n 4`) — equivalent to TestNG's `parallel="methods"` configuration. All tests use Faker-generated data, so there's no shared mutable state.

### Test Data Factory
`test_data_factory.py` uses the Python `Faker` library to generate unique, realistic payloads on every run — eliminating data collisions in parallel execution.

### Response Time Validation
`api_utils.assert_response_time(response, max_ms)` uses `requests`' built-in `response.elapsed` timing and fails with a clear message if the SLA is breached.

---

## CI/CD Integration (GitHub Actions example)

```yaml
name: API Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.11'
      - run: pip install -r requirements.txt
      - run: pytest --alluredir=allure-results
        env:
          BASE_URL: ${{ secrets.API_BASE_URL }}
          AUTH_ADMIN_TOKEN: ${{ secrets.ADMIN_TOKEN }}
          AUTH_USER_TOKEN: ${{ secrets.USER_TOKEN }}
      - name: Publish Allure Report
        uses: simple-elf/allure-report-action@master
        with:
          allure_results: allure-results
```

---

## Original Framework

This is a Python port of an equivalent framework originally built in **Java 11 · Rest Assured · TestNG · Allure · Log4j2**, kept 1:1 in test-case coverage and structure.
