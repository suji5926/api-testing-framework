"""
config_manager.py — Singleton config loader.
Reads config/config.ini and exposes values as attributes,
with environment-variable overrides (upper-case) taking priority.
Mirrors the Java ConfigManager.java.
"""

import os
import configparser
from pathlib import Path


class ConfigManager:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._load()
        return cls._instance

    def _load(self):
        parser = configparser.ConfigParser()
        config_path = Path(__file__).resolve().parents[2] / "config" / "config.ini"
        parser.read(config_path)

        defaults = parser["DEFAULT"] if "DEFAULT" in parser else {}

        self.base_url = os.getenv("BASE_URL", defaults.get("base_url", ""))
        self.base_path = self.base_url
        self.api_version = os.getenv("API_VERSION", defaults.get("api_version", "v1"))
        self.timeout_seconds = int(
            os.getenv("TIMEOUT_SECONDS", defaults.get("timeout_seconds", "3"))
        )
        self.environment = os.getenv("ENVIRONMENT", defaults.get("environment", "qa"))

        self.auth_admin_token = os.getenv(
            "AUTH_ADMIN_TOKEN", defaults.get("auth_admin_token", "")
        )
        self.auth_user_token = os.getenv(
            "AUTH_USER_TOKEN", defaults.get("auth_user_token", "")
        )
        self.auth_invalid_token = os.getenv(
            "AUTH_INVALID_TOKEN", defaults.get("auth_invalid_token", "invalid.token.value")
        )


# Singleton instance used across the framework
config = ConfigManager()
