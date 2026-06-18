package com.apitest.utils;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * ApiUtils — assertion helpers and response-inspection shortcuts
 * that keep test methods clean and DRY.
 */
public class ApiUtils {

    private static final Logger log = LogManager.getLogger(ApiUtils.class);

    // ── Status code assertions ────────────────────────────────────────────────

    public static void assertStatusCode(Response response, int expected) {
        int actual = response.getStatusCode();
        log.info("Asserting status code — expected: {}, actual: {}", expected, actual);
        Assert.assertEquals(actual, expected,
                "Unexpected status code. Body: " + response.getBody().asString());
    }

    public static void assertOk(Response response)          { assertStatusCode(response, 200); }
    public static void assertCreated(Response response)     { assertStatusCode(response, 201); }
    public static void assertNoContent(Response response)   { assertStatusCode(response, 204); }
    public static void assertBadRequest(Response response)  { assertStatusCode(response, 400); }
    public static void assertUnauthorized(Response response){ assertStatusCode(response, 401); }
    public static void assertForbidden(Response response)   { assertStatusCode(response, 403); }
    public static void assertNotFound(Response response)    { assertStatusCode(response, 404); }

    // ── Response-time assertions ──────────────────────────────────────────────

    /**
     * Asserts the response was received within {@code maxMs} milliseconds.
     * Logs a warning rather than failing when the limit is only slightly breached.
     */
    public static void assertResponseTime(Response response, long maxMs) {
        long actual = response.getTime();
        log.info("Response time: {} ms (limit: {} ms)", actual, maxMs);
        if (actual > maxMs) {
            String msg = String.format("Response time %d ms exceeded limit of %d ms", actual, maxMs);
            Assert.fail(msg);
        }
    }

    public static void assertFastResponse(Response response) {
        assertResponseTime(response, 2_000);
    }

    // ── Field extraction helpers ──────────────────────────────────────────────

    public static <T> T extractField(Response response, String jsonPath, Class<T> type) {
        T value = response.jsonPath().getObject(jsonPath, type);
        log.info("Extracted '{}' = {}", jsonPath, value);
        Assert.assertNotNull(value, "Field '" + jsonPath + "' was null in response");
        return value;
    }

    public static String extractString(Response response, String jsonPath) {
        return extractField(response, jsonPath, String.class);
    }

    public static Integer extractInt(Response response, String jsonPath) {
        return extractField(response, jsonPath, Integer.class);
    }

    // ── Content-type assertion ────────────────────────────────────────────────

    public static void assertJsonContentType(Response response) {
        String ct = response.getContentType();
        log.info("Content-Type: {}", ct);
        Assert.assertTrue(ct.contains("application/json"),
                "Expected JSON content type, got: " + ct);
    }

    // ── Generic not-null body assertion ──────────────────────────────────────

    public static void assertBodyNotEmpty(Response response) {
        String body = response.getBody().asString();
        Assert.assertNotNull(body, "Response body is null");
        Assert.assertFalse(body.isEmpty(), "Response body is empty");
    }
}
