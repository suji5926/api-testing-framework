package com.apitest.utils;

import com.apitest.models.AuthModels;
import com.apitest.models.User;
import com.github.javafaker.Faker;

import java.util.HashMap;
import java.util.Map;

/**
 * TestDataFactory — generates realistic, randomised test payloads.
 * All methods are static; no state is held between calls.
 */
public class TestDataFactory {

    private static final Faker faker = new Faker();

    // ── User payloads ─────────────────────────────────────────────────────────

    /** Fully-populated valid user. */
    public static User validUser() {
        User user = new User(
                faker.name().fullName(),
                faker.internet().emailAddress(),
                faker.name().username()
        );
        user.setPhone(faker.phoneNumber().phoneNumber());
        user.setWebsite(faker.internet().domainName());
        return user;
    }

    /** User with name intentionally omitted — triggers 400 validation error. */
    public static User userMissingName() {
        User user = new User();
        user.setEmail(faker.internet().emailAddress());
        user.setUsername(faker.name().username());
        return user;
    }

    /** User with name intentionally omitted — triggers 400 validation error. */
    public static User userMissingEmail() {
        User user = new User();
        user.setName(faker.name().fullName());
        user.setUsername(faker.name().username());
        return user;
    }

    /** User with invalid e-mail format. */
    public static User userInvalidEmail() {
        User user = validUser();
        user.setEmail("not-a-valid-email@@broken");
        return user;
    }

    /** Completely empty body. */
    public static Map<String, Object> emptyPayload() {
        return new HashMap<>();
    }

    // ── Auth payloads ─────────────────────────────────────────────────────────

    public static AuthModels.LoginRequest validCredentials() {
        return new AuthModels.LoginRequest("admin", "password123");
    }

    public static AuthModels.LoginRequest invalidCredentials() {
        return new AuthModels.LoginRequest(
                faker.name().username(),
                faker.internet().password()
        );
    }

    public static AuthModels.LoginRequest missingPassword() {
        return new AuthModels.LoginRequest(faker.name().username(), null);
    }

    public static AuthModels.LoginRequest missingUsername() {
        return new AuthModels.LoginRequest(null, faker.internet().password());
    }

    // ── Generic helpers ───────────────────────────────────────────────────────

    /** Returns a map ready to be serialised as a partial-update (PATCH) body. */
    public static Map<String, Object> partialUserUpdate() {
        Map<String, Object> patch = new HashMap<>();
        patch.put("name",  faker.name().fullName());
        patch.put("email", faker.internet().emailAddress());
        return patch;
    }

    /** Generates a random non-existent resource ID for 404 tests. */
    public static int nonExistentId() {
        return 999_999 + faker.number().numberBetween(1, 999);
    }
}
