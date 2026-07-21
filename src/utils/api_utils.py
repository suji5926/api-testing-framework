"""
api_utils.py — assertion helpers and response-inspection shortcuts
that keep test functions clean and DRY. Mirrors the Java ApiUtils.java.
"""

import logging
from requests import Response

log = logging.getLogger("api_utils")


# ── Status code assertions ──────────────────────────────────────────────────

def assert_status_code(response: Response, expected: int):
    actual = response.status_code
    log.info(f"Asserting status code — expected: {expected}, actual: {actual}")
    assert actual == expected, (
        f"Unexpected status code. Expected {expected}, got {actual}. "
        f"Body: {response.text}"
    )


def assert_ok(response: Response):           assert_status_code(response, 200)
def assert_created(response: Response):      assert_status_code(response, 201)
def assert_no_content(response: Response):   assert_status_code(response, 204)
def assert_bad_request(response: Response):  assert_status_code(response, 400)
def assert_unauthorized(response: Response): assert_status_code(response, 401)
def assert_forbidden(response: Response):    assert_status_code(response, 403)
def assert_not_found(response: Response):    assert_status_code(response, 404)


# ── Response-time assertions ─────────────────────────────────────────────────

def assert_response_time(response: Response, max_ms: int):
    """Asserts the response was received within max_ms milliseconds."""
    actual_ms = response.elapsed.total_seconds() * 1000
    log.info(f"Response time: {actual_ms:.0f} ms (limit: {max_ms} ms)")
    assert actual_ms <= max_ms, (
        f"Response time {actual_ms:.0f} ms exceeded limit of {max_ms} ms"
    )


def assert_fast_response(response: Response):
    assert_response_time(response, 2_000)


# ── Field extraction helpers ────────────────────────────────────────────────

def extract_field(response: Response, key: str):
    value = response.json().get(key)
    log.info(f"Extracted '{key}' = {value}")
    assert value is not None, f"Field '{key}' was null in response"
    return value


def extract_string(response: Response, key: str) -> str:
    return str(extract_field(response, key))


def extract_int(response: Response, key: str) -> int:
    return int(extract_field(response, key))


# ── Content-type assertion ──────────────────────────────────────────────────

def assert_json_content_type(response: Response):
    ct = response.headers.get("Content-Type", "")
    log.info(f"Content-Type: {ct}")
    assert "application/json" in ct, f"Expected JSON content type, got: {ct}"


# ── Generic not-null body assertion ─────────────────────────────────────────

def assert_body_not_empty(response: Response):
    body = response.text
    assert body is not None, "Response body is null"
    assert body != "", "Response body is empty"
