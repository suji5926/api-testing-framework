package com.apitest.tests;

import com.apitest.config.BaseTest;
import com.apitest.models.User;
import com.apitest.utils.ApiUtils;
import com.apitest.utils.TestDataFactory;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;

@Epic("API Testing Framework")
@Feature("PUT & DELETE Requests")
public class PutDeleteApiTest extends BaseTest {

    private static final String USERS_ENDPOINT  = "/users";
    private static final String USER_BY_ID      = "/users/{id}";
    private static final int    EXISTING_USER_ID = 1;

    // =========================================================================
    // PUT TESTS
    // =========================================================================

    @Test(groups = {"smoke", "regression"})
    @Story("Valid PUT - full update")
    @Description("PUT /users/{id} with a complete payload returns 200 with updated data")
    @Severity(SeverityLevel.CRITICAL)
    public void updateUser_validFullPayload_returns200() {
        log.info("TC-PUT-001: Full update of user ID {}", EXISTING_USER_ID);
        User updated = TestDataFactory.validUser();

        Response response = given()
                .spec(authAdminSpec)
                .pathParam("id", EXISTING_USER_ID)
                .body(updated)
                .when()
                .put(USER_BY_ID)
                .then()
                .extract().response();

        ApiUtils.assertOk(response);
        ApiUtils.assertJsonContentType(response);
        Assert.assertEquals(response.jsonPath().getString("name"),  updated.getName());
        Assert.assertEquals(response.jsonPath().getString("email"), updated.getEmail());
        log.info("TC-PUT-001 PASS");
    }

    @Test(groups = {"regression"})
    @Story("Valid PUT - partial update (PATCH semantics)")
    @Description("PATCH /users/{id} with partial fields returns 200 with merged data")
    @Severity(SeverityLevel.NORMAL)
    public void updateUser_partialPayload_returns200() {
        log.info("TC-PUT-002: Partial update of user ID {}", EXISTING_USER_ID);
        Map<String, Object> patch = TestDataFactory.partialUserUpdate();

        Response response = given()
                .spec(authAdminSpec)
                .pathParam("id", EXISTING_USER_ID)
                .body(patch)
                .when()
                .patch(USER_BY_ID)
                .then()
                .extract().response();

        ApiUtils.assertOk(response);
        Assert.assertEquals(response.jsonPath().getString("name"),  patch.get("name"));
        Assert.assertEquals(response.jsonPath().getString("email"), patch.get("email"));
    }

    @Test(groups = {"regression", "negative"})
    @Story("Invalid PUT - non-existent resource")
    @Description("PUT /users/{id} for an ID that does not exist returns 404")
    @Severity(SeverityLevel.NORMAL)
    public void updateUser_nonExistentId_returns404() {
        log.info("TC-PUT-003: PUT to non-existent ID");
        int id = TestDataFactory.nonExistentId();

        Response response = given()
                .spec(authAdminSpec)
                .pathParam("id", id)
                .body(TestDataFactory.validUser())
                .when()
                .put(USER_BY_ID)
                .then()
                .extract().response();

        ApiUtils.assertNotFound(response);
    }

    @Test(groups = {"regression", "negative"})
    @Story("Invalid PUT - empty body")
    @Description("PUT /users/{id} with an empty JSON body returns 400")
    @Severity(SeverityLevel.NORMAL)
    public void updateUser_emptyBody_returns400() {
        log.info("TC-PUT-004: PUT with empty body");

        Response response = given()
                .spec(authAdminSpec)
                .pathParam("id", EXISTING_USER_ID)
                .body(TestDataFactory.emptyPayload())
                .when()
                .put(USER_BY_ID)
                .then()
                .extract().response();

        ApiUtils.assertBadRequest(response);
    }

    @Test(groups = {"regression", "auth"})
    @Story("Unauthorised PUT - no token")
    @Description("PUT /users/{id} without Authorization header returns 401")
    @Severity(SeverityLevel.CRITICAL)
    public void updateUser_noToken_returns401() {
        log.info("TC-PUT-005: PUT without auth token");

        Response response = given()
                .spec(noAuthSpec)
                .pathParam("id", EXISTING_USER_ID)
                .body(TestDataFactory.validUser())
                .when()
                .put(USER_BY_ID)
                .then()
                .extract().response();

        ApiUtils.assertUnauthorized(response);
    }

    @Test(groups = {"performance"})
    @Story("Response time - PUT endpoint")
    @Description("PUT /users/{id} must respond within 3000 ms")
    @Severity(SeverityLevel.NORMAL)
    public void updateUser_responseTime_under3Seconds() {
        log.info("TC-PUT-006: Response time for PUT /users/{id}");

        Response response = given()
                .spec(authAdminSpec)
                .pathParam("id", EXISTING_USER_ID)
                .body(TestDataFactory.validUser())
                .when()
                .put(USER_BY_ID)
                .then()
                .extract().response();

        ApiUtils.assertOk(response);
        ApiUtils.assertResponseTime(response, 3_000);
    }

    // =========================================================================
    // DELETE TESTS
    // =========================================================================

    @Test(groups = {"smoke", "regression"})
    @Story("Valid DELETE - existing resource")
    @Description("DELETE /users/{id} for an existing resource returns 200 or 204")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteUser_existingId_returnsSuccess() {
        log.info("TC-DEL-001: DELETE existing user ID {}", EXISTING_USER_ID);

        Response response = given()
                .spec(authAdminSpec)
                .pathParam("id", EXISTING_USER_ID)
                .when()
                .delete(USER_BY_ID)
                .then()
                .extract().response();

        int status = response.getStatusCode();
        Assert.assertTrue(status == 200 || status == 204,
                "Expected 200 or 204 on DELETE, got " + status);
        log.info("TC-DEL-001 PASS — status: {}", status);
    }

    @Test(groups = {"regression", "negative"})
    @Story("Invalid DELETE - non-existent resource")
    @Description("DELETE /users/{id} for a non-existent ID returns 404")
    @Severity(SeverityLevel.NORMAL)
    public void deleteUser_nonExistentId_returns404() {
        log.info("TC-DEL-002: DELETE non-existent ID");
        int id = TestDataFactory.nonExistentId();

        Response response = given()
                .spec(authAdminSpec)
                .pathParam("id", id)
                .when()
                .delete(USER_BY_ID)
                .then()
                .extract().response();

        ApiUtils.assertNotFound(response);
    }

    @Test(groups = {"regression", "auth"})
    @Story("Unauthorised DELETE - no token")
    @Description("DELETE /users/{id} without Authorization header returns 401")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteUser_noToken_returns401() {
        log.info("TC-DEL-003: DELETE without auth token");

        Response response = given()
                .spec(noAuthSpec)
                .pathParam("id", EXISTING_USER_ID)
                .when()
                .delete(USER_BY_ID)
                .then()
                .extract().response();

        ApiUtils.assertUnauthorized(response);
    }

    @Test(groups = {"regression", "auth"})
    @Story("Forbidden DELETE - insufficient role")
    @Description("DELETE /users/{id} with a user-role token returns 403")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteUser_userRoleToken_returns403() {
        log.info("TC-DEL-004: DELETE with user-role token");

        Response response = given()
                .spec(authUserSpec)
                .pathParam("id", EXISTING_USER_ID)
                .when()
                .delete(USER_BY_ID)
                .then()
                .extract().response();

        ApiUtils.assertForbidden(response);
    }

    @Test(groups = {"performance"})
    @Story("Response time - DELETE endpoint")
    @Description("DELETE /users/{id} must respond within 2000 ms")
    @Severity(SeverityLevel.NORMAL)
    public void deleteUser_responseTime_under2Seconds() {
        log.info("TC-DEL-005: Response time for DELETE /users/{id}");

        Response response = given()
                .spec(authAdminSpec)
                .pathParam("id", EXISTING_USER_ID)
                .when()
                .delete(USER_BY_ID)
                .then()
                .extract().response();

        int status = response.getStatusCode();
        Assert.assertTrue(status == 200 || status == 204, "Unexpected status: " + status);
        ApiUtils.assertResponseTime(response, 2_000);
    }
}
