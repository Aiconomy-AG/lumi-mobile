#!/usr/bin/env python3
"""Scan documentation for high-risk secret and private configuration patterns."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TARGETS = [ROOT / "docs", ROOT / "README.md", ROOT / "mkdocs.yml", ROOT / "site"]

PATTERNS = [
    re.compile(r"AIza[0-9A-Za-z_-]{20,}"),
    re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH |)PRIVATE KEY-----"),
    re.compile(r"\b(?:password|passwd|secret|token|api[_-]?key)\s*[:=]\s*['\"]?[A-Za-z0-9_./+=:-]{8,}", re.IGNORECASE),
    re.compile(r"\bhttps?://(?:localhost|127\.0\.0\.1|10\.|172\.(?:1[6-9]|2\d|3[0-1])\.|192\.168\.)", re.IGNORECASE),
]

ALLOWED_SUBSTRINGS = {
    "token = user.token",
    "Authorization: Bearer <token>",
    "REVERB_APP_KEY",
    "API_BASE_URL",
    "API_VERSION",
}


def files() -> list[Path]:
    result: list[Path] = []
    for target in TARGETS:
        if target.is_dir():
            result.extend(sorted(target.rglob("*")))
        elif target.exists():
            result.append(target)
    return [
        path
        for path in result
        if path.is_file() and not path.relative_to(ROOT).as_posix().startswith("site/assets/")
    ]


def main() -> int:
    findings: list[str] = []
    for path in files():
        text = path.read_text(encoding="utf-8", errors="ignore")
        for index, line in enumerate(text.splitlines(), start=1):
            if any(allowed in line for allowed in ALLOWED_SUBSTRINGS):
                continue
            for pattern in PATTERNS:
                if pattern.search(line):
                    findings.append(f"{path.relative_to(ROOT)}:{index}: {line.strip()}")

    if findings:
        print("Potential sensitive content found in documentation:")
        for finding in findings:
            print(f"  - {finding}")
        return 1

    print("Documentation sensitive-content scan passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
