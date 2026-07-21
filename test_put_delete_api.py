"""
test_put_delete_api.py — mirrors PutDeleteApiTest.java (11 cases).
"""

import logging
import allure
import pytest

from src.utils import api_utils, test_data_factory as tdf

log = logging.getLogger(__name__)

USER_BY_ID = "/users/{id}"
EXISTING_USER_ID = 1


@allure.epic("API Testing Framework")
@allure.feature("PUT & DELETE Requests")
class TestPutDeleteApi:

    # ==========================================================================
    # PUT TESTS
    # ==========================================================================

    @pytest.mark.smoke
    @pytest.mark.regression
    @allure.story("Valid PUT - full update")
    @allure.description("PUT /users/{id} with a complete payload returns 200 with updated data")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_update_user_valid_full_payload_returns_200(self, auth_admin_spec, base_path):
        log.info(f"TC-PUT-001: Full update of user ID {EXISTING_USER_ID}")
        updated = tdf.valid_user()
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_admin_spec.put(url, json=updated.to_json())

        api_utils.assert_ok(response)
        api_utils.assert_json_content_type(response)
        body = response.json()
        assert body.get("name") == updated.name
        assert body.get("email") == updated.email
        log.info("TC-PUT-001 PASS")

    @pytest.mark.regression
    @allure.story("Valid PUT - partial update (PATCH semantics)")
    @allure.description("PATCH /users/{id} with partial fields returns 200 with merged data")
    @allure.severity(allure.severity_level.NORMAL)
    def test_update_user_partial_payload_returns_200(self, auth_admin_spec, base_path):
        log.info(f"TC-PUT-002: Partial update of user ID {EXISTING_USER_ID}")
        patch = tdf.partial_user_update()
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_admin_spec.patch(url, json=patch)

        api_utils.assert_ok(response)
        body = response.json()
        assert body.get("name") == patch["name"]
        assert body.get("email") == patch["email"]

    @pytest.mark.regression
    @pytest.mark.negative
    @allure.story("Invalid PUT - non-existent resource")
    @allure.description("PUT /users/{id} for an ID that does not exist returns 404")
    @allure.severity(allure.severity_level.NORMAL)
    def test_update_user_non_existent_id_returns_404(self, auth_admin_spec, base_path):
        log.info("TC-PUT-003: PUT to non-existent ID")
        non_existent_id = tdf.non_existent_id()
        url = f"{base_path}{USER_BY_ID}".format(id=non_existent_id)
        response = auth_admin_spec.put(url, json=tdf.valid_user().to_json())

        api_utils.assert_not_found(response)

    @pytest.mark.regression
    @pytest.mark.negative
    @allure.story("Invalid PUT - empty body")
    @allure.description("PUT /users/{id} with an empty JSON body returns 400")
    @allure.severity(allure.severity_level.NORMAL)
    def test_update_user_empty_body_returns_400(self, auth_admin_spec, base_path):
        log.info("TC-PUT-004: PUT with empty body")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_admin_spec.put(url, json=tdf.empty_payload())

        api_utils.assert_bad_request(response)

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Unauthorised PUT - no token")
    @allure.description("PUT /users/{id} without Authorization header returns 401")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_update_user_no_token_returns_401(self, no_auth_spec, base_path):
        log.info("TC-PUT-005: PUT without auth token")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = no_auth_spec.put(url, json=tdf.valid_user().to_json())

        api_utils.assert_unauthorized(response)

    @pytest.mark.performance
    @allure.story("Response time - PUT endpoint")
    @allure.description("PUT /users/{id} must respond within 3000 ms")
    @allure.severity(allure.severity_level.NORMAL)
    def test_update_user_response_time_under_3_seconds(self, auth_admin_spec, base_path):
        log.info("TC-PUT-006: Response time for PUT /users/{id}")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_admin_spec.put(url, json=tdf.valid_user().to_json())

        api_utils.assert_ok(response)
        api_utils.assert_response_time(response, 3_000)

    # ==========================================================================
    # DELETE TESTS
    # ==========================================================================

    @pytest.mark.smoke
    @pytest.mark.regression
    @allure.story("Valid DELETE - existing resource")
    @allure.description("DELETE /users/{id} for an existing resource returns 200 or 204")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_delete_user_existing_id_returns_success(self, auth_admin_spec, base_path):
        log.info(f"TC-DEL-001: DELETE existing user ID {EXISTING_USER_ID}")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_admin_spec.delete(url)

        assert response.status_code in (200, 204), \
            f"Expected 200 or 204 on DELETE, got {response.status_code}"
        log.info(f"TC-DEL-001 PASS — status: {response.status_code}")

    @pytest.mark.regression
    @pytest.mark.negative
    @allure.story("Invalid DELETE - non-existent resource")
    @allure.description("DELETE /users/{id} for a non-existent ID returns 404")
    @allure.severity(allure.severity_level.NORMAL)
    def test_delete_user_non_existent_id_returns_404(self, auth_admin_spec, base_path):
        log.info("TC-DEL-002: DELETE non-existent ID")
        non_existent_id = tdf.non_existent_id()
        url = f"{base_path}{USER_BY_ID}".format(id=non_existent_id)
        response = auth_admin_spec.delete(url)

        api_utils.assert_not_found(response)

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Unauthorised DELETE - no token")
    @allure.description("DELETE /users/{id} without Authorization header returns 401")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_delete_user_no_token_returns_401(self, no_auth_spec, base_path):
        log.info("TC-DEL-003: DELETE without auth token")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = no_auth_spec.delete(url)

        api_utils.assert_unauthorized(response)

    @pytest.mark.regression
    @pytest.mark.auth
    @allure.story("Forbidden DELETE - insufficient role")
    @allure.description("DELETE /users/{id} with a user-role token returns 403")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_delete_user_user_role_token_returns_403(self, auth_user_spec, base_path):
        log.info("TC-DEL-004: DELETE with user-role token")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_user_spec.delete(url)

        api_utils.assert_forbidden(response)

    @pytest.mark.performance
    @allure.story("Response time - DELETE endpoint")
    @allure.description("DELETE /users/{id} must respond within 2000 ms")
    @allure.severity(allure.severity_level.NORMAL)
    def test_delete_user_response_time_under_2_seconds(self, auth_admin_spec, base_path):
        log.info("TC-DEL-005: Response time for DELETE /users/{id}")
        url = f"{base_path}{USER_BY_ID}".format(id=EXISTING_USER_ID)
        response = auth_admin_spec.delete(url)

        assert response.status_code in (200, 204), f"Unexpected status: {response.status_code}"
        api_utils.assert_response_time(response, 2_000)
