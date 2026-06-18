package com.apitest.tests;

import com.apitest.config.BaseTest;
import com.apitest.models.AuthModels;
import com.apitest.utils.ApiUtils;
import com.apitest.utils.TestDataFactory;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * AuthApiTest — covers login success/failure, token validation,
 * token refresh, and logout flows.
 */
@Epic("API Testing Framework")
@Feature("Authentication APIs")
public class AuthApiTest extends BaseTest {

    private static final String LOGIN_ENDPOINT   = "/auth/login";
    private static final String REFRESH_ENDPOINT = "/auth/token/refresh";
    private static final String LOGOUT_ENDPOINT  = "/auth/logout";
    private static final String PROFILE_ENDPOINT = "/auth/profile";

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN — POSITIVE TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"smoke", "regression", "auth"})
    @Story("Valid login - correct credentials")
    @Description("POST /auth/login with valid username and password returns 200 with a token")
    @Severity(SeverityLevel.BLOCKER)
    public void login_validCredentials_returns200WithToken() {
        log.info("TC-AUTH-001: Login with valid credentials");

        Response response = given()
                .spec(requestSpec)
                .body(TestDataFactory.validCredentials())
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertOk(response);
        ApiUtils.assertJsonContentType(response);
        ApiUtils.assertFastResponse(response);

        String token = ApiUtils.extractString(response, "token");
        Assert.assertFalse(token.isEmpty(), "Token must not be empty");
        Assert.assertNotNull(response.jsonPath().get("userId"),    "userId must be present");
        Assert.assertNotNull(response.jsonPath().get("expiresIn"), "expiresIn must be present");
        log.info("TC-AUTH-001 PASS — token obtained (length {})", token.length());
    }

    @Test(groups = {"regression", "auth"})
    @Story("Valid login - token is usable")
    @Description("Token obtained at login must authenticate a subsequent API request")
    @Severity(SeverityLevel.CRITICAL)
    public void login_obtainedToken_canAuthenticateRequest() {
        log.info("TC-AUTH-002: Verify login token works on a protected endpoint");

        // Step 1 — obtain token
        Response loginResp = given()
                .spec(requestSpec)
                .body(TestDataFactory.validCredentials())
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertOk(loginResp);
        String token = ApiUtils.extractString(loginResp, "token");

        // Step 2 — use token on a protected endpoint
        Response profileResp = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .when()
                .get(PROFILE_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertOk(profileResp);
        log.info("TC-AUTH-002 PASS — token used successfully");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN — NEGATIVE TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"regression", "negative", "auth"})
    @Story("Invalid login - wrong password")
    @Description("POST /auth/login with wrong password returns 401")
    @Severity(SeverityLevel.CRITICAL)
    public void login_invalidCredentials_returns401() {
        log.info("TC-AUTH-003: Login with invalid credentials");

        Response response = given()
                .spec(requestSpec)
                .body(TestDataFactory.invalidCredentials())
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertUnauthorized(response);
        Assert.assertNull(response.jsonPath().get("token"), "No token should be issued on failed login");
    }

    @Test(groups = {"regression", "negative", "auth"})
    @Story("Invalid login - missing password")
    @Description("POST /auth/login without 'password' field returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    public void login_missingPassword_returns400() {
        log.info("TC-AUTH-004: Login without password");

        Response response = given()
                .spec(requestSpec)
                .body(TestDataFactory.missingPassword())
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertBadRequest(response);
    }

    @Test(groups = {"regression", "negative", "auth"})
    @Story("Invalid login - missing username")
    @Description("POST /auth/login without 'username' field returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    public void login_missingUsername_returns400() {
        log.info("TC-AUTH-005: Login without username");

        Response response = given()
                .spec(requestSpec)
                .body(TestDataFactory.missingUsername())
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertBadRequest(response);
    }

    @Test(groups = {"regression", "negative", "auth"})
    @Story("Invalid login - empty body")
    @Description("POST /auth/login with empty JSON body returns 400")
    @Severity(SeverityLevel.NORMAL)
    public void login_emptyBody_returns400() {
        log.info("TC-AUTH-006: Login with empty body");

        Response response = given()
                .spec(requestSpec)
                .body(TestDataFactory.emptyPayload())
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertBadRequest(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOKEN VALIDATION
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"regression", "auth"})
    @Story("Token validation - expired/invalid token")
    @Description("A request with an invalid token returns 401 on a protected endpoint")
    @Severity(SeverityLevel.CRITICAL)
    public void protectedEndpoint_withInvalidToken_returns401() {
        log.info("TC-AUTH-007: Access protected endpoint with invalid token");

        Response response = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + config.getInvalidToken())
                .when()
                .get(PROFILE_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertUnauthorized(response);
    }

    @Test(groups = {"regression", "auth"})
    @Story("Token validation - missing token")
    @Description("A request with no Authorization header returns 401")
    @Severity(SeverityLevel.CRITICAL)
    public void protectedEndpoint_withNoToken_returns401() {
        log.info("TC-AUTH-008: Access protected endpoint without token");

        Response response = given()
                .spec(noAuthSpec)
                .when()
                .get(PROFILE_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertUnauthorized(response);
    }

    @Test(groups = {"regression", "auth"})
    @Story("Token validation - malformed Authorization header")
    @Description("'Authorization: Token xyz' (wrong scheme) returns 401")
    @Severity(SeverityLevel.NORMAL)
    public void protectedEndpoint_wrongAuthScheme_returns401() {
        log.info("TC-AUTH-009: Wrong Authorization scheme");

        Response response = given()
                .spec(requestSpec)
                .header("Authorization", "Token " + config.getAdminToken())
                .when()
                .get(PROFILE_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertUnauthorized(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOKEN REFRESH
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"regression", "auth"})
    @Story("Token refresh - valid refresh token")
    @Description("POST /auth/token/refresh with a valid refresh token returns a new access token")
    @Severity(SeverityLevel.CRITICAL)
    public void refreshToken_validToken_returnsNewAccessToken() {
        log.info("TC-AUTH-010: Token refresh with valid refresh token");

        AuthModels.RefreshRequest refreshReq =
                new AuthModels.RefreshRequest(config.getAdminToken());

        Response response = given()
                .spec(requestSpec)
                .body(refreshReq)
                .when()
                .post(REFRESH_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertOk(response);
        String newToken = ApiUtils.extractString(response, "token");
        Assert.assertFalse(newToken.isEmpty(), "Refreshed token must not be empty");
        log.info("TC-AUTH-010 PASS — new token length: {}", newToken.length());
    }

    @Test(groups = {"regression", "negative", "auth"})
    @Story("Token refresh - invalid refresh token")
    @Description("POST /auth/token/refresh with an invalid token returns 401")
    @Severity(SeverityLevel.NORMAL)
    public void refreshToken_invalidToken_returns401() {
        log.info("TC-AUTH-011: Token refresh with invalid token");

        AuthModels.RefreshRequest refreshReq =
                new AuthModels.RefreshRequest(config.getInvalidToken());

        Response response = given()
                .spec(requestSpec)
                .body(refreshReq)
                .when()
                .post(REFRESH_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertUnauthorized(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"regression", "auth"})
    @Story("Logout - valid session")
    @Description("POST /auth/logout with a valid token returns 200 or 204 and invalidates the token")
    @Severity(SeverityLevel.NORMAL)
    public void logout_validToken_returnsSuccessAndInvalidatesToken() {
        log.info("TC-AUTH-012: Logout with valid token");

        Response logoutResp = given()
                .spec(authAdminSpec)
                .when()
                .post(LOGOUT_ENDPOINT)
                .then()
                .extract().response();

        int status = logoutResp.getStatusCode();
        Assert.assertTrue(status == 200 || status == 204,
                "Expected 200 or 204 on logout, got " + status);

        // After logout the token must no longer work
        Response profileResp = given()
                .spec(authAdminSpec)
                .when()
                .get(PROFILE_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertUnauthorized(profileResp);
        log.info("TC-AUTH-012 PASS — token invalidated after logout");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PERFORMANCE
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"performance"})
    @Story("Response time - login endpoint")
    @Description("POST /auth/login must respond within 3000 ms")
    @Severity(SeverityLevel.NORMAL)
    public void login_responseTime_under3Seconds() {
        log.info("TC-AUTH-013: Login response time");

        Response response = given()
                .spec(requestSpec)
                .body(TestDataFactory.validCredentials())
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertOk(response);
        ApiUtils.assertResponseTime(response, 3_000);
    }
}
