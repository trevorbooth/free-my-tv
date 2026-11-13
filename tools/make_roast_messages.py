#!/usr/bin/env python3
"""Builds app/assets/roast/messages.json (the lines Roast Mode types on the home screen and the screensaver) from
tools/roast_messages.txt: one hand-written message per line, blank lines and lines starting with # skipped.

    python3 tools/make_roast_messages.py

Every message must fit in three lines on screen (120 characters at most); duplicates are dropped. Standard library only.
"""
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE = os.path.join(ROOT, "tools", "roast_messages.txt")
OUT = os.path.join(ROOT, "app", "assets", "roast", "messages.json")
MAX = 120


def main():
    lines, problems = [], []
    with open(SOURCE, encoding="utf-8") as f:
        for n, raw in enumerate(f, 1):
            s = " ".join(raw.split())
            if not s or s.startswith("#"):
                continue
            if len(s) > MAX:
                problems.append(f"line {n}: {len(s)} characters (at most {MAX}): {s}")
            elif s not in lines:
                lines.append(s)
    if problems:
        print("\n".join(problems))
        return 1
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump({"lines": lines}, f, ensure_ascii=False, indent=0)
        f.write("\n")
    print(f"{len(lines)} messages -> {os.path.relpath(OUT, ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
