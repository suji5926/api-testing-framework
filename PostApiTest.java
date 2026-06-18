package com.apitest.tests;

import com.apitest.config.BaseTest;
import com.apitest.models.User;
import com.apitest.utils.ApiUtils;
import com.apitest.utils.TestDataFactory;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Epic("API Testing Framework")
@Feature("POST Requests")
public class PostApiTest extends BaseTest {

    private static final String USERS_ENDPOINT = "/users";

    // ─────────────────────────────────────────────────────────────────────────
    // POSITIVE TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"smoke", "regression"})
    @Story("Valid POST - create user")
    @Description("POST /users with valid payload returns 201 with created resource")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_validPayload_returns201() {
        log.info("TC-POST-001: Create user with valid payload");
        User user = TestDataFactory.validUser();

        Response response = given()
                .spec(authAdminSpec)
                .body(user)
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertCreated(response);
        ApiUtils.assertJsonContentType(response);
        ApiUtils.assertFastResponse(response);

        Assert.assertNotNull(response.jsonPath().get("id"), "Response must include generated ID");
        Assert.assertEquals(response.jsonPath().getString("name"), user.getName());
        Assert.assertEquals(response.jsonPath().getString("email"), user.getEmail());
        log.info("TC-POST-001 PASS — created user ID: {}", response.jsonPath().get("id"));
    }

    @Test(groups = {"regression"})
    @Story("Valid POST - response contains all submitted fields")
    @Description("The response body must echo back all fields submitted in the request")
    @Severity(SeverityLevel.NORMAL)
    public void createUser_verifyEchoedFields() {
        log.info("TC-POST-002: Verify echoed fields in creation response");
        User user = TestDataFactory.validUser();

        Response response = given()
                .spec(authAdminSpec)
                .body(user)
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertCreated(response);
        Assert.assertEquals(response.jsonPath().getString("name"),     user.getName(),     "name mismatch");
        Assert.assertEquals(response.jsonPath().getString("email"),    user.getEmail(),    "email mismatch");
        Assert.assertEquals(response.jsonPath().getString("username"), user.getUsername(), "username mismatch");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NEGATIVE TESTS — missing mandatory fields
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"regression", "negative"})
    @Story("Invalid POST - missing name field")
    @Description("POST /users without 'name' returns 400 Bad Request")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_missingName_returns400() {
        log.info("TC-POST-003: Create user with missing 'name'");

        Response response = given()
                .spec(authAdminSpec)
                .body(TestDataFactory.userMissingName())
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertBadRequest(response);
    }

    @Test(groups = {"regression", "negative"})
    @Story("Invalid POST - missing email field")
    @Description("POST /users without 'email' returns 400 Bad Request")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_missingEmail_returns400() {
        log.info("TC-POST-004: Create user with missing 'email'");

        Response response = given()
                .spec(authAdminSpec)
                .body(TestDataFactory.userMissingEmail())
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertBadRequest(response);
    }

    @Test(groups = {"regression", "negative"})
    @Story("Invalid POST - empty body")
    @Description("POST /users with an empty JSON body returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    public void createUser_emptyBody_returns400() {
        log.info("TC-POST-005: Create user with empty body");

        Response response = given()
                .spec(authAdminSpec)
                .body(TestDataFactory.emptyPayload())
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertBadRequest(response);
    }

    @Test(groups = {"regression", "negative"})
    @Story("Invalid POST - invalid email format")
    @Description("POST /users with a malformed email returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    public void createUser_invalidEmailFormat_returns400() {
        log.info("TC-POST-006: Create user with invalid email format");

        Response response = given()
                .spec(authAdminSpec)
                .body(TestDataFactory.userInvalidEmail())
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertBadRequest(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTHENTICATION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"regression", "auth"})
    @Story("Unauthorised POST - no token")
    @Description("POST /users without Authorization header returns 401")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_noToken_returns401() {
        log.info("TC-POST-007: POST without auth token");

        Response response = given()
                .spec(noAuthSpec)
                .body(TestDataFactory.validUser())
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertUnauthorized(response);
    }

    @Test(groups = {"regression", "auth"})
    @Story("Forbidden POST - insufficient role")
    @Description("POST /users with a user-role token (not admin) returns 403")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_userRoleToken_returns403() {
        log.info("TC-POST-008: POST with user-role token");

        Response response = given()
                .spec(authUserSpec)
                .body(TestDataFactory.validUser())
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertForbidden(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PERFORMANCE TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = {"performance"})
    @Story("Response time - POST endpoint")
    @Description("POST /users must respond within 3000 ms")
    @Severity(SeverityLevel.NORMAL)
    public void createUser_responseTime_under3Seconds() {
        log.info("TC-POST-009: Response time for POST /users");

        Response response = given()
                .spec(authAdminSpec)
                .body(TestDataFactory.validUser())
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract().response();

        ApiUtils.assertCreated(response);
        ApiUtils.assertResponseTime(response, 3_000);
    }
}
