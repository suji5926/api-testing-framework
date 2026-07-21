"""
test_data_factory.py — generates realistic, randomised test payloads.
Mirrors the Java TestDataFactory.java (JavaFaker -> Python Faker).
All functions are stateless.
"""

import random
from faker import Faker

from src.models.user import User
from src.models.auth_models import LoginRequest, RefreshRequest

faker = Faker()


# ── User payloads ────────────────────────────────────────────────────────────

def valid_user() -> User:
    return User(
        name=faker.name(),
        email=faker.email(),
        username=faker.user_name(),
        phone=faker.phone_number(),
        website=faker.domain_name(),
    )


def user_missing_name() -> User:
    return User(email=faker.email(), username=faker.user_name())


def user_missing_email() -> User:
    return User(name=faker.name(), username=faker.user_name())


def user_invalid_email() -> User:
    user = valid_user()
    user.email = "not-a-valid-email@@broken"
    return user


def empty_payload() -> dict:
    return {}


# ── Auth payloads ────────────────────────────────────────────────────────────

def valid_credentials() -> LoginRequest:
    return LoginRequest(username="admin", password="password123")


def invalid_credentials() -> LoginRequest:
    return LoginRequest(username=faker.user_name(), password=faker.password())


def missing_password() -> LoginRequest:
    return LoginRequest(username=faker.user_name(), password=None)


def missing_username() -> LoginRequest:
    return LoginRequest(username=None, password=faker.password())


# ── Generic helpers ─────────────────────────────────────────────────────────

def partial_user_update() -> dict:
    return {"name": faker.name(), "email": faker.email()}


def non_existent_id() -> int:
    return 999_999 + random.randint(1, 999)
