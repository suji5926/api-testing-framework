"""
auth_models.py — Auth request/response dataclasses.
Mirrors the Java AuthModels.java.
"""

from dataclasses import dataclass, asdict
from typing import Optional


@dataclass
class LoginRequest:
    username: Optional[str] = None
    password: Optional[str] = None

    def to_json(self) -> dict:
        return {k: v for k, v in asdict(self).items() if v is not None}


@dataclass
class RefreshRequest:
    refresh_token: Optional[str] = None

    def to_json(self) -> dict:
        return {k: v for k, v in asdict(self).items() if v is not None}
