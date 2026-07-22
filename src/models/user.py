"""
user.py — User request/response dataclass.
Mirrors the Java User.java.
"""

from dataclasses import dataclass, asdict
from typing import Optional


@dataclass
class User:
    name: Optional[str] = None
    email: Optional[str] = None
    username: Optional[str] = None
    phone: Optional[str] = None
    website: Optional[str] = None

    def to_json(self) -> dict:
        """Return only the fields that are set, dropping None values."""
        return {k: v for k, v in asdict(self).items() if v is not None}
