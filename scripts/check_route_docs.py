#!/usr/bin/env python3
"""Verify that Compose sections and subroutes are mentioned in routing docs."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP_SECTION = ROOT / "shared/src/commonMain/kotlin/org/example/project/presentation/menu/AppSection.kt"
SUB_ROUTE = ROOT / "shared/src/commonMain/kotlin/org/example/project/presentation/menu/MainSubRoute.kt"
ROUTING_DOC = ROOT / "docs/architecture/routing.md"


def read(path: Path) -> str:
    if not path.exists():
        raise SystemExit(f"Missing expected file: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def app_sections(source: str) -> list[str]:
    body = source.split("enum class AppSection", 1)[1]
    names = []
    for line in body.splitlines():
        match = re.match(r"\s*([A-Z][A-Z0-9_]*)\(", line)
        if match:
            names.append(match.group(1))
    return names


def subroutes(source: str) -> list[str]:
    return re.findall(r"data (?:object|class) ([A-Za-z][A-Za-z0-9_]*)", source)


def humanize(value: str) -> str:
    return value.replace("_", " ").title()


def main() -> int:
    doc = read(ROUTING_DOC).lower()
    missing: list[str] = []

    for section in app_sections(read(APP_SECTION)):
        if section.lower() not in doc and humanize(section).lower() not in doc:
            missing.append(f"AppSection.{section}")

    for route in subroutes(read(SUB_ROUTE)):
        if route.lower() not in doc and humanize(route).lower() not in doc:
            missing.append(f"MainSubRoute.{route}")

    if missing:
        print("Missing route documentation entries:")
        for item in missing:
            print(f"  - {item}")
        return 1

    print("Route documentation coverage check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

