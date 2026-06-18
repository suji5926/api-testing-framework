package com.apitest.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;

/**
 * TestListener — hooks into the TestNG lifecycle to log
 * test outcomes clearly and emit a summary at suite end.
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    // ── Suite ─────────────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  SUITE START: {}",               suite.getName());
        log.info("╚══════════════════════════════════════════════════════════╝");
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  SUITE FINISH: {}",              suite.getName());
        log.info("╚══════════════════════════════════════════════════════════╝");
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        log.info("▶ START  | {}", getTestName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✔ PASS   | {} [{} ms]",
                getTestName(result), getDuration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("✘ FAIL   | {} [{} ms]",
                getTestName(result), getDuration(result));
        if (result.getThrowable() != null) {
            log.error("  Cause: {}", result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⊘ SKIP   | {}", getTestName(result));
        if (result.getThrowable() != null) {
            log.warn("  Reason: {}", result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.warn("~ PARTIAL | {}", getTestName(result));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getTestName(ITestResult result) {
        return result.getTestClass().getSimpleName() + "." + result.getName();
    }

    private long getDuration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
