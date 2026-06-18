package com.apitest.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import static org.hamcrest.Matchers.lessThan;

/**
 * BaseTest — all test classes extend this.
 * Configures Rest Assured specifications and Allure logging once per suite.
 */
public class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);
    protected static final ConfigManager config = ConfigManager.getInstance();

    protected static RequestSpecification requestSpec;
    protected static RequestSpecification authAdminSpec;
    protected static RequestSpecification authUserSpec;
    protected static RequestSpecification noAuthSpec;
    protected static ResponseSpecification responseSpec;

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        log.info("=== Suite Setup | Environment: {} | Base: {} ===",
                config.getEnvironment(), config.getBasePath());

        RestAssured.baseURI = config.getBaseUrl();
        RestAssured.basePath = "/" + config.getApiVersion();

        // ── Base request spec (shared headers) ──────────────────────────────
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setBasePath("/" + config.getApiVersion())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.ALL)
                .build();

        // ── Auth variants ────────────────────────────────────────────────────
        authAdminSpec = new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + config.getAdminToken())
                .build();

        authUserSpec = new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + config.getUserToken())
                .build();

        noAuthSpec = new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                // deliberately no Authorization header
                .build();

        // ── Response spec (global assertions) ───────────────────────────────
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan((long) (config.getTimeout() * 1000L)))
                .log(LogDetail.ALL)
                .build();

        log.info("Rest Assured specs initialised successfully");
    }

    @AfterSuite(alwaysRun = true)
    public void globalTeardown() {
        log.info("=== Suite Teardown complete ===");
        RestAssured.reset();
    }
}
