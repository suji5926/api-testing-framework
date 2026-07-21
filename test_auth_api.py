"""
test_auth_api.py — covers login success/failure, token validation,
token refresh, and logout flows. Mirrors AuthApiTest.java (13 cases).
"""

import logging
import allure
import pytest

from src.config.config_manager import config
from src.utils import api_utils, test_data_factory as tdf

log = logging.getLogger(__name__)

LOGIN_ENDPOINT = "/auth/login"
REFRESH_ENDPOINT = "/auth/token/refresh"
LOGOUT_ENDPOINT = "/auth/logout"
PROFILE_ENDPOINT = "/auth/profile"


@allure.epic("API Testing Framework")
@allure.feature("Authentication APIs")
class TestAuthApi:

    # ── LOGIN — POSITIVE ─────────────────────────────────────────────────

    @pytest.mark.smoke
    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Valid login - correct credentials")
    @allure.description("POST /auth/login with valid username and password returns 200 with a token")
    @allure.severity(allure.severity_level.BLOCKER)
    def test_login_valid_credentials_returns_200_with_token(self, request_spec, base_path):
        log.info("TC-AUTH-001: Login with valid credentials")
        response = request_spec.post(f"{base_path}{LOGIN_ENDPOINT}",
                                      json=tdf.valid_credentials().to_json())

        api_utils.assert_ok(response)
        api_utils.assert_json_content_type(response)
        api_utils.assert_fast_response(response)

        token = api_utils.extract_string(response, "token")
        assert token != "", "Token must not be empty"
        assert response.json().get("userId") is not None, "userId must be present"
        assert response.json().get("expiresIn") is not None, "expiresIn must be present"
        log.info(f"TC-AUTH-001 PASS — token obtained (length {len(token)})")

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Valid login - token is usable")
    @allure.description("Token obtained at login must authenticate a subsequent API request")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_login_obtained_token_can_authenticate_request(self, request_spec, base_path):
        log.info("TC-AUTH-002: Verify login token works on a protected endpoint")

        login_resp = request_spec.post(f"{base_path}{LOGIN_ENDPOINT}",
                                        json=tdf.valid_credentials().to_json())
        api_utils.assert_ok(login_resp)
        token = api_utils.extract_string(login_resp, "token")

        profile_resp = request_spec.get(
            f"{base_path}{PROFILE_ENDPOINT}",
            headers={"Authorization": f"Bearer {token}"},
        )
        api_utils.assert_ok(profile_resp)
        log.info("TC-AUTH-002 PASS — token used successfully")

    # ── LOGIN — NEGATIVE ─────────────────────────────────────────────────

    @pytest.mark.regression
    @pytest.mark.negative
    @pytest.mark.auth
    @allure.story("Invalid login - wrong password")
    @allure.description("POST /auth/login with wrong password returns 401")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_login_invalid_credentials_returns_401(self, request_spec, base_path):
        log.info("TC-AUTH-003: Login with invalid credentials")
        response = request_spec.post(f"{base_path}{LOGIN_ENDPOINT}",
                                      json=tdf.invalid_credentials().to_json())

        api_utils.assert_unauthorized(response)
        assert response.json().get("token") is None, "No token should be issued on failed login"

    @pytest.mark.regression
    @pytest.mark.negative
    @pytest.mark.auth
    @allure.story("Invalid login - missing password")
    @allure.description("POST /auth/login without 'password' field returns 400 Bad Request")
    @allure.severity(allure.severity_level.NORMAL)
    def test_login_missing_password_returns_400(self, request_spec, base_path):
        log.info("TC-AUTH-004: Login without password")
        response = request_spec.post(f"{base_path}{LOGIN_ENDPOINT}",
                                      json=tdf.missing_password().to_json())
        api_utils.assert_bad_request(response)

    @pytest.mark.regression
    @pytest.mark.negative
    @pytest.mark.auth
    @allure.story("Invalid login - missing username")
    @allure.description("POST /auth/login without 'username' field returns 400 Bad Request")
    @allure.severity(allure.severity_level.NORMAL)
    def test_login_missing_username_returns_400(self, request_spec, base_path):
        log.info("TC-AUTH-005: Login without username")
        response = request_spec.post(f"{base_path}{LOGIN_ENDPOINT}",
                                      json=tdf.missing_username().to_json())
        api_utils.assert_bad_request(response)

    @pytest.mark.regression
    @pytest.mark.negative
    @pytest.mark.auth
    @allure.story("Invalid login - empty body")
    @allure.description("POST /auth/login with empty JSON body returns 400")
    @allure.severity(allure.severity_level.NORMAL)
    def test_login_empty_body_returns_400(self, request_spec, base_path):
        log.info("TC-AUTH-006: Login with empty body")
        response = request_spec.post(f"{base_path}{LOGIN_ENDPOINT}", json=tdf.empty_payload())
        api_utils.assert_bad_request(response)

    # ── TOKEN VALIDATION ─────────────────────────────────────────────────

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Token validation - expired/invalid token")
    @allure.description("A request with an invalid token returns 401 on a protected endpoint")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_protected_endpoint_with_invalid_token_returns_401(self, request_spec, base_path):
        log.info("TC-AUTH-007: Access protected endpoint with invalid token")
        response = request_spec.get(
            f"{base_path}{PROFILE_ENDPOINT}",
            headers={"Authorization": f"Bearer {config.invalid_token}"},
        )
        api_utils.assert_unauthorized(response)

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Token validation - missing token")
    @allure.description("A request with no Authorization header returns 401")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_protected_endpoint_with_no_token_returns_401(self, no_auth_spec, base_path):
        log.info("TC-AUTH-008: Access protected endpoint without token")
        response = no_auth_spec.get(f"{base_path}{PROFILE_ENDPOINT}")
        api_utils.assert_unauthorized(response)

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Token validation - malformed Authorization header")
    @allure.description("'Authorization: Token xyz' (wrong scheme) returns 401")
    @allure.severity(allure.severity_level.NORMAL)
    def test_protected_endpoint_wrong_auth_scheme_returns_401(self, request_spec, base_path):
        log.info("TC-AUTH-009: Wrong Authorization scheme")
        response = request_spec.get(
            f"{base_path}{PROFILE_ENDPOINT}",
            headers={"Authorization": f"Token {config.admin_token}"},
        )
        api_utils.assert_unauthorized(response)

    # ── TOKEN REFRESH ────────────────────────────────────────────────────

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Token refresh - valid refresh token")
    @allure.description("POST /auth/token/refresh with a valid refresh token returns a new access token")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_refresh_token_valid_token_returns_new_access_token(self, request_spec, base_path):
        log.info("TC-AUTH-010: Token refresh with valid refresh token")
        from src.models.auth_models import RefreshRequest
        refresh_req = RefreshRequest(refreshToken=config.admin_token)

        response = request_spec.post(f"{base_path}{REFRESH_ENDPOINT}", json=refresh_req.to_json())
        api_utils.assert_ok(response)
        new_token = api_utils.extract_string(response, "token")
        assert new_token != "", "Refreshed token must not be empty"
        log.info(f"TC-AUTH-010 PASS — new token length: {len(new_token)}")

    @pytest.mark.regression
    @pytest.mark.negative
    @pytest.mark.auth
    @allure.story("Token refresh - invalid refresh token")
    @allure.description("POST /auth/token/refresh with an invalid token returns 401")
    @allure.severity(allure.severity_level.NORMAL)
    def test_refresh_token_invalid_token_returns_401(self, request_spec, base_path):
        log.info("TC-AUTH-011: Token refresh with invalid token")
        from src.models.auth_models import RefreshRequest
        refresh_req = RefreshRequest(refreshToken=config.invalid_token)

        response = request_spec.post(f"{base_path}{REFRESH_ENDPOINT}", json=refresh_req.to_json())
        api_utils.assert_unauthorized(response)

    # ── LOGOUT ───────────────────────────────────────────────────────────

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Logout - valid session")
    @allure.description("POST /auth/logout with a valid token returns 200 or 204 and invalidates the token")
    @allure.severity(allure.severity_level.NORMAL)
    def test_logout_valid_token_returns_success_and_invalidates_token(self, auth_admin_spec, base_path):
        log.info("TC-AUTH-012: Logout with valid token")
        logout_resp = auth_admin_spec.post(f"{base_path}{LOGOUT_ENDPOINT}")
        assert logout_resp.status_code in (200, 204), \
            f"Expected 200 or 204 on logout, got {logout_resp.status_code}"

        profile_resp = auth_admin_spec.get(f"{base_path}{PROFILE_ENDPOINT}")
        api_utils.assert_unauthorized(profile_resp)
        log.info("TC-AUTH-012 PASS — token invalidated after logout")

    # ── PERFORMANCE ──────────────────────────────────────────────────────

    @pytest.mark.performance
    @allure.story("Response time - login endpoint")
    @allure.description("POST /auth/login must respond within 3000 ms")
    @allure.severity(allure.severity_level.NORMAL)
    def test_login_response_time_under_3_seconds(self, request_spec, base_path):
        log.info("TC-AUTH-013: Login response time")
        response = request_spec.post(f"{base_path}{LOGIN_ENDPOINT}",
                                      json=tdf.valid_credentials().to_json())
        api_utils.assert_ok(response)
        api_utils.assert_response_time(response, 3_000)
