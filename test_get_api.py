"""
test_get_api.py — GET endpoint coverage for /users.
Mirrors the GetApiTest.java referenced in the original README
(not included in the uploaded Java files, rebuilt here to match
the documented 10-case coverage: 2 smoke, 8 regression, 2 auth, 2 negative, 2 performance).
"""

import logging
import allure
import pytest

from src.utils import api_utils, test_data_factory as tdf

log = logging.getLogger(__name__)

USERS_ENDPOINT = "/users"
USER_BY_ID = "/users/{id}"
EXISTING_USER_ID = 1


@allure.epic("API Testing Framework")
@allure.feature("GET Requests")
class TestGetApi:

    # ── POSITIVE ─────────────────────────────────────────────────────────

    @pytest.mark.smoke
    @pytest.mark.regression
    @allure.story("Valid GET - list all users")
    @allure.description("GET /users returns 200 with a JSON array of users")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_get_all_users_returns_200(self, auth_admin_spec, base_path):
        log.info("TC-GET-001: List all users")
        response = auth_admin_spec.get(f"{base_path}{USERS_ENDPOINT}")

        api_utils.assert_ok(response)
        api_utils.assert_json_content_type(response)
        api_utils.assert_fast_response(response)
        assert isinstance(response.json(), list), "Response body must be a JSON array"

    @pytest.mark.smoke
    @pytest.mark.regression
    @allure.story("Valid GET - single user by ID")
    @allure.description("GET /users/{id} for an existing user returns 200 with matching data")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_get_user_by_existing_id_returns_200(self, auth_admin_spec, base_path):
        log.info("TC-GET-002: Get user by existing ID")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_admin_spec.get(url)

        api_utils.assert_ok(response)
        assert response.json().get("id") == EXISTING_USER_ID

    @pytest.mark.regression
    @allure.story("Valid GET - response schema fields present")
    @allure.description("GET /users/{id} response must contain name, email, and username")
    @allure.severity(allure.severity_level.NORMAL)
    def test_get_user_response_contains_required_fields(self, auth_admin_spec, base_path):
        log.info("TC-GET-003: Verify required fields in user response")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_admin_spec.get(url)

        api_utils.assert_ok(response)
        body = response.json()
        for field in ("name", "email", "username"):
            assert body.get(field) is not None, f"'{field}' must be present in response"

    @pytest.mark.regression
    @allure.story("Valid GET - pagination")
    @allure.description("GET /users?page=1&limit=10 returns a limited, correctly paginated result set")
    @allure.severity(allure.severity_level.NORMAL)
    def test_get_users_with_pagination(self, auth_admin_spec, base_path):
        log.info("TC-GET-004: Paginated GET /users")
        response = auth_admin_spec.get(f"{base_path}{USERS_ENDPOINT}", params={"page": 1, "limit": 10})

        api_utils.assert_ok(response)
        assert len(response.json()) <= 10, "Pagination limit was not respected"

    @pytest.mark.regression
    @allure.story("Valid GET - filter by query param")
    @allure.description("GET /users?username=<value> returns only matching users")
    @allure.severity(allure.severity_level.NORMAL)
    def test_get_users_filtered_by_username(self, auth_admin_spec, base_path):
        log.info("TC-GET-005: Filtered GET /users by username")
        response = auth_admin_spec.get(f"{base_path}{USERS_ENDPOINT}", params={"username": "admin"})

        api_utils.assert_ok(response)
        results = response.json()
        assert all(u.get("username") == "admin" for u in results), "Filter returned non-matching users"

    @pytest.mark.regression
    @allure.story("Valid GET - nested address object")
    @allure.description("GET /users/{id} includes a nested address object when present")
    @allure.severity(allure.severity_level.NORMAL)
    def test_get_user_includes_nested_address(self, auth_admin_spec, base_path):
        log.info("TC-GET-006: Verify nested address object")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_admin_spec.get(url)

        api_utils.assert_ok(response)
        # Address is optional data — only assert structure if present
        address = response.json().get("address")
        if address is not None:
            assert "city" in address, "Nested address must include 'city'"

    # ── NEGATIVE ─────────────────────────────────────────────────────────

    @pytest.mark.regression
    @pytest.mark.negative
    @allure.story("Invalid GET - non-existent resource")
    @allure.description("GET /users/{id} for an ID that does not exist returns 404")
    @allure.severity(allure.severity_level.NORMAL)
    def test_get_user_non_existent_id_returns_404(self, auth_admin_spec, base_path):
        log.info("TC-GET-007: GET non-existent user ID")
        non_existent_id = tdf.non_existent_id()
        url = f"{base_path}{USER_BY_ID}".format(id=non_existent_id)
        response = auth_admin_spec.get(url)

        api_utils.assert_not_found(response)

    @pytest.mark.regression
    @pytest.mark.negative
    @allure.story("Invalid GET - malformed ID")
    @allure.description("GET /users/{id} with a non-numeric ID returns 400")
    @allure.severity(allure.severity_level.NORMAL)
    def test_get_user_malformed_id_returns_400(self, auth_admin_spec, base_path):
        log.info("TC-GET-008: GET with malformed (non-numeric) ID")
        url = f"{base_path}{USER_BY_ID}".format(id="abc")
        response = auth_admin_spec.get(url)

        api_utils.assert_bad_request(response)

    # ── AUTH ─────────────────────────────────────────────────────────────

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Unauthorised GET - no token")
    @allure.description("GET /users without Authorization header returns 401")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_get_users_no_token_returns_401(self, no_auth_spec, base_path):
        log.info("TC-GET-009: GET /users without auth token")
        response = no_auth_spec.get(f"{base_path}{USERS_ENDPOINT}")
        api_utils.assert_unauthorized(response)

    # ── PERFORMANCE ──────────────────────────────────────────────────────

    @pytest.mark.performance
    @allure.story("Response time - GET endpoint")
    @allure.description("GET /users must respond within 2000 ms")
    @allure.severity(allure.severity_level.NORMAL)
    def test_get_users_response_time_under_2_seconds(self, auth_admin_spec, base_path):
        log.info("TC-GET-010: Response time for GET /users")
        response = auth_admin_spec.get(f"{base_path}{USERS_ENDPOINT}")

        api_utils.assert_ok(response)
        api_utils.assert_response_time(response, 2_000)
