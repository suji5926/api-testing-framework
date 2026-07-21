"""
test_post_api.py — mirrors PostApiTest.java (9 cases).
"""

import logging
import allure
import pytest

from src.utils import api_utils, test_data_factory as tdf

log = logging.getLogger(__name__)

USERS_ENDPOINT = "/users"


@allure.epic("API Testing Framework")
@allure.feature("POST Requests")
class TestPostApi:

    # ── POSITIVE ─────────────────────────────────────────────────────────

    @pytest.mark.smoke
    @pytest.mark.regression
    @allure.story("Valid POST - create user")
    @allure.description("POST /users with valid payload returns 201 with created resource")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_create_user_valid_payload_returns_201(self, auth_admin_spec, base_path):
        log.info("TC-POST-001: Create user with valid payload")
        user = tdf.valid_user()
        response = auth_admin_spec.post(f"{base_path}{USERS_ENDPOINT}", json=user.to_json())

        api_utils.assert_created(response)
        api_utils.assert_json_content_type(response)
        api_utils.assert_fast_response(response)

        body = response.json()
        assert body.get("id") is not None, "Response must include generated ID"
        assert body.get("name") == user.name
        assert body.get("email") == user.email
        log.info(f"TC-POST-001 PASS — created user ID: {body.get('id')}")

    @pytest.mark.regression
    @allure.story("Valid POST - response contains all submitted fields")
    @allure.description("The response body must echo back all fields submitted in the request")
    @allure.severity(allure.severity_level.NORMAL)
    def test_create_user_verify_echoed_fields(self, auth_admin_spec, base_path):
        log.info("TC-POST-002: Verify echoed fields in creation response")
        user = tdf.valid_user()
        response = auth_admin_spec.post(f"{base_path}{USERS_ENDPOINT}", json=user.to_json())

        api_utils.assert_created(response)
        body = response.json()
        assert body.get("name") == user.name, "name mismatch"
        assert body.get("email") == user.email, "email mismatch"
        assert body.get("username") == user.username, "username mismatch"

    # ── NEGATIVE — missing mandatory fields ──────────────────────────────

    @pytest.mark.regression
    @pytest.mark.negative
    @allure.story("Invalid POST - missing name field")
    @allure.description("POST /users without 'name' returns 400 Bad Request")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_create_user_missing_name_returns_400(self, auth_admin_spec, base_path):
        log.info("TC-POST-003: Create user with missing 'name'")
        response = auth_admin_spec.post(f"{base_path}{USERS_ENDPOINT}",
                                         json=tdf.user_missing_name().to_json())
        api_utils.assert_bad_request(response)

    @pytest.mark.regression
    @pytest.mark.negative
    @allure.story("Invalid POST - missing email field")
    @allure.description("POST /users without 'email' returns 400 Bad Request")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_create_user_missing_email_returns_400(self, auth_admin_spec, base_path):
        log.info("TC-POST-004: Create user with missing 'email'")
        response = auth_admin_spec.post(f"{base_path}{USERS_ENDPOINT}",
                                         json=tdf.user_missing_email().to_json())
        api_utils.assert_bad_request(response)

    @pytest.mark.regression
    @pytest.mark.negative
    @allure.story("Invalid POST - empty body")
    @allure.description("POST /users with an empty JSON body returns 400 Bad Request")
    @allure.severity(allure.severity_level.NORMAL)
    def test_create_user_empty_body_returns_400(self, auth_admin_spec, base_path):
        log.info("TC-POST-005: Create user with empty body")
        response = auth_admin_spec.post(f"{base_path}{USERS_ENDPOINT}", json=tdf.empty_payload())
        api_utils.assert_bad_request(response)

    @pytest.mark.regression
    @pytest.mark.negative
    @allure.story("Invalid POST - invalid email format")
    @allure.description("POST /users with a malformed email returns 400 Bad Request")
    @allure.severity(allure.severity_level.NORMAL)
    def test_create_user_invalid_email_format_returns_400(self, auth_admin_spec, base_path):
        log.info("TC-POST-006: Create user with invalid email format")
        response = auth_admin_spec.post(f"{base_path}{USERS_ENDPOINT}",
                                         json=tdf.user_invalid_email().to_json())
        api_utils.assert_bad_request(response)

    # ── AUTH ─────────────────────────────────────────────────────────────

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Unauthorised POST - no token")
    @allure.description("POST /users without Authorization header returns 401")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_create_user_no_token_returns_401(self, no_auth_spec, base_path):
        log.info("TC-POST-007: POST without auth token")
        response = no_auth_spec.post(f"{base_path}{USERS_ENDPOINT}", json=tdf.valid_user().to_json())
        api_utils.assert_unauthorized(response)

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Forbidden POST - insufficient role")
    @allure.description("POST /users with a user-role token (not admin) returns 403")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_create_user_user_role_token_returns_403(self, auth_user_spec, base_path):
        log.info("TC-POST-008: POST with user-role token")
        response = auth_user_spec.post(f"{base_path}{USERS_ENDPOINT}", json=tdf.valid_user().to_json())
        api_utils.assert_forbidden(response)

    # ── PERFORMANCE ──────────────────────────────────────────────────────

    @pytest.mark.performance
    @allure.story("Response time - POST endpoint")
    @allure.description("POST /users must respond within 3000 ms")
    @allure.severity(allure.severity_level.NORMAL)
    def test_create_user_response_time_under_3_seconds(self, auth_admin_spec, base_path):
        log.info("TC-POST-009: Response time for POST /users")
        response = auth_admin_spec.post(f"{base_path}{USERS_ENDPOINT}", json=tdf.valid_user().to_json())

        api_utils.assert_created(response)
        api_utils.assert_response_time(response, 3_000)
