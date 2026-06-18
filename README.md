# API Testing Framework

**Stack:** Java 11 · Rest Assured 5.4 · TestNG 7.9 · Allure Reports · Log4j2

A production-ready REST API testing framework demonstrating skills that matter most to recruiters: authentication flows, positive/negative coverage, response-time validation, parallel execution, and structured reporting.

---

## Project Structure

```
api-testing-framework/
├── pom.xml
└── src/test/
    ├── java/com/apitest/
    │   ├── config/
    │   │   ├── BaseTest.java          # Rest Assured specs + suite setup
    │   │   └── ConfigManager.java     # Singleton config loader
    │   ├── models/
    │   │   ├── User.java              # Request/response POJO
    │   │   └── AuthModels.java        # Auth request/response POJOs
    │   ├── utils/
    │   │   ├── ApiUtils.java          # Assertion helpers
    │   │   └── TestDataFactory.java   # Faker-powered test data
    │   ├── listeners/
    │   │   └── TestListener.java      # TestNG lifecycle logger
    │   └── tests/
    │       ├── GetApiTest.java        # 10 GET test cases
    │       ├── PostApiTest.java       # 9 POST test cases
    │       ├── PutDeleteApiTest.java  # 11 PUT/DELETE test cases
    │       └── AuthApiTest.java       # 13 Auth test cases
    └── resources/
        ├── config.properties
        ├── testng.xml
        └── log4j2.xml
```

---

## Test Case Coverage (43 cases total)

| Area | Smoke | Regression | Auth | Negative | Performance |
|---|---|---|---|---|---|
| GET | 2 | 8 | 2 | 2 | 2 |
| POST | 1 | 8 | 2 | 4 | 1 |
| PUT | 1 | 5 | 1 | 2 | 1 |
| DELETE | 1 | 4 | 2 | 1 | 1 |
| Auth | 2 | 13 | 9 | 5 | 1 |

### Scenario types covered
- ✅ Valid request (happy path)
- ✅ Invalid request (malformed data, wrong types)
- ✅ Missing mandatory fields (name, email, password, username)
- ✅ Unauthorized access (no token, invalid token, wrong scheme, expired token)
- ✅ Insufficient permissions (user role attempting admin action)
- ✅ Response time validation (per-endpoint SLA assertions)
- ✅ Resource not found (non-existent IDs)
- ✅ Token lifecycle (obtain → use → refresh → logout)

---

## Quick Start

### 1. Prerequisites
- Java 11+
- Maven 3.8+

### 2. Configure
Edit `src/test/resources/config.properties`:
```properties
base.url=https://your-api.example.com
auth.admin.token=<your_admin_jwt>
auth.user.token=<your_user_jwt>
```

### 3. Run tests

```bash
# Full suite
mvn test

# Smoke tests only
mvn test -Dgroups=smoke

# Against a different environment
mvn test -Dbase.url=https://staging.example.com -Denvironment=staging

# Single test class
mvn test -Dtest=AuthApiTest

# Generate Allure report
mvn allure:report
open target/site/allure-maven/index.html
```

---

## Key Framework Features

### Shared Request Specifications
`BaseTest` builds four reusable specs once at suite start:
- `authAdminSpec` — Bearer token with admin privileges
- `authUserSpec` — Bearer token with user privileges
- `noAuthSpec` — No Authorization header (for 401 tests)
- `requestSpec` — Base spec (content-type, logging, Allure filter)

### Allure Reporting
Every test is annotated with `@Epic`, `@Feature`, `@Story`, `@Description`, and `@Severity`. The Allure filter on the request spec captures full request/response payloads into the report automatically.

### Parallel Execution
`testng.xml` runs methods in parallel with 4 threads — no shared mutable state is used, so tests are fully thread-safe.

### Test Data Factory
`TestDataFactory` uses JavaFaker to generate unique, realistic payloads on every run — eliminating data collisions in parallel execution and making tests independent of seed data.

### Response Time Validation
`ApiUtils.assertResponseTime(response, maxMs)` checks actual elapsed time from Rest Assured's built-in timing and fails with a clear message if the SLA is breached.

---

## CI / CD Integration

```yaml
# GitHub Actions example
- name: Run API Tests
  run: mvn test -Dbase.url=${{ secrets.API_BASE_URL }}
                -Dauth.admin.token=${{ secrets.ADMIN_TOKEN }}
                -Dauth.user.token=${{ secrets.USER_TOKEN }}

- name: Publish Allure Report
  uses: simple-elf/allure-report-action@master
  with:
    allure_results: target/allure-results
```
