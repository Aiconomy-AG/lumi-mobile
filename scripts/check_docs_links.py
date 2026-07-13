#!/usr/bin/env python3
"""Check relative internal Markdown links in docs and README."""

from __future__ import annotations

import re
import sys
from pathlib import Path
from urllib.parse import unquote, urlparse

ROOT = Path(__file__).resolve().parents[1]
DOC_ROOTS = [ROOT / "docs", ROOT / "README.md"]
LINK_RE = re.compile(r"(?<!!)\[[^\]]+\]\(([^)]+)\)")
HEADING_RE = re.compile(r"^#+\s+(.+?)\s*$", re.MULTILINE)


def slugify(text: str) -> str:
    text = re.sub(r"\s+\{#.*?\}\s*$", "", text.strip())
    text = re.sub(r"[^\w\s-]", "", text.lower())
    text = re.sub(r"\s+", "-", text)
    return text.strip("-")


def markdown_files() -> list[Path]:
    files = sorted((ROOT / "docs").rglob("*.md"))
    readme = ROOT / "README.md"
    if readme.exists():
        files.append(readme)
    return files


def anchors(path: Path) -> set[str]:
    text = path.read_text(encoding="utf-8")
    return {slugify(match.group(1)) for match in HEADING_RE.finditer(text)}


def main() -> int:
    known_anchors = {path: anchors(path) for path in markdown_files()}
    errors: list[str] = []

    for path in markdown_files():
        text = path.read_text(encoding="utf-8")
        for raw_link in LINK_RE.findall(text):
            if raw_link.startswith(("http://", "https://", "mailto:", "#")):
                continue
            parsed = urlparse(raw_link)
            if parsed.scheme:
                continue
            target_text = unquote(parsed.path)
            if not target_text:
                continue
            target = (path.parent / target_text).resolve()
            if ROOT not in target.parents and target != ROOT:
                errors.append(f"{path.relative_to(ROOT)} links outside repo: {raw_link}")
                continue
            if not target.exists():
                errors.append(f"{path.relative_to(ROOT)} has missing link target: {raw_link}")
                continue
            if parsed.fragment and target.suffix.lower() == ".md":
                if parsed.fragment not in known_anchors.get(target, set()):
                    errors.append(
                        f"{path.relative_to(ROOT)} has missing anchor '{parsed.fragment}' in {target.relative_to(ROOT)}"
                    )

    if errors:
        print("Broken internal documentation links:")
        for error in errors:
            print(f"  - {error}")
        return 1

    print("Internal documentation link check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

