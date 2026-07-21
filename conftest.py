"""
conftest.py — shared pytest fixtures and hooks.

Combines the responsibilities of the Java BaseTest.java (request specs)
and TestListener.java (test-result logging).
"""

import logging
import requests
import pytest

from src.config.config_manager import config

log = logging.getLogger("suite")


# ── Suite-level setup/teardown (equivalent of @BeforeSuite/@AfterSuite) ────

@pytest.fixture(scope="session", autouse=True)
def suite_setup_teardown():
    log.info("=" * 60)
    log.info(f"SUITE START | Environment: {config.environment} | Base: {config.base_path}")
    log.info("=" * 60)
    yield
    log.info("=" * 60)
    log.info("SUITE FINISH")
    log.info("=" * 60)


# ── Base URL fixture ─────────────────────────────────────────────────────────

@pytest.fixture(scope="session")
def base_path() -> str:
    return config.base_path


# ── Session fixtures (equivalent of Rest Assured's RequestSpecification) ───

@pytest.fixture(scope="session")
def request_spec():
    """Base session — content-type/accept headers, no auth."""
    session = requests.Session()
    session.headers.update({
        "Content-Type": "application/json",
        "Accept": "application/json",
    })
    return session


@pytest.fixture(scope="session")
def auth_admin_spec():
    session = requests.Session()
    session.headers.update({
        "Content-Type": "application/json",
        "Accept": "application/json",
        "Authorization": f"Bearer {config.admin_token}",
    })
    return session


@pytest.fixture(scope="session")
def auth_user_spec():
    session = requests.Session()
    session.headers.update({
        "Content-Type": "application/json",
        "Accept": "application/json",
        "Authorization": f"Bearer {config.user_token}",
    })
    return session


@pytest.fixture(scope="session")
def no_auth_spec():
    """Deliberately no Authorization header — for 401 tests."""
    session = requests.Session()
    session.headers.update({
        "Content-Type": "application/json",
        "Accept": "application/json",
    })
    return session


# ── Test-result logging hook (equivalent of TestListener.java) ─────────────

@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()

    if report.when == "call":
        name = f"{item.parent.name}::{item.name}"
        duration_ms = report.duration * 1000

        if report.passed:
            log.info(f"✔ PASS  | {name} [{duration_ms:.0f} ms]")
        elif report.failed:
            log.error(f"✘ FAIL  | {name} [{duration_ms:.0f} ms]")
            if call.excinfo:
                log.error(f"  Cause: {call.excinfo.value}")
        elif report.skipped:
            log.warning(f"⊘ SKIP  | {name}")
