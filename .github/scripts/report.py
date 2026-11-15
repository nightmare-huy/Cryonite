#!/usr/bin/env python3
"""Publishes condensed Gradle logs as check runs + job summary so failures are readable without downloading logs."""
import json
import os
import re
import sys
import urllib.request

LIMIT = 60000
INTERESTING = re.compile(
    r"error:|warning: \[|FAILED|FAILURE|What went wrong|Exception|Caused by|\bERROR\b|/ERROR\]|\[WARN|/WARN\]|"
    r"at dev\.nightmarehuy|BUILD |Missing|Couldn't|Could not|Unknown|Parsing error|Failed|failed|GameTest|tests? (passed|failed)|required tests",
)


def condense(path: str) -> str:
    with open(path, errors="replace") as fh:
        lines = fh.read().splitlines()
    picked, keep = [], 0
    for line in lines:
        if INTERESTING.search(line):
            keep = 4
        if keep > 0:
            picked.append(line[:600])
            keep -= 1
    tail = [l[:600] for l in lines[-60:]]
    text = "\n".join(picked)
    if len(text) > LIMIT - 8000:
        text = text[: LIMIT - 8000] + "\n... (truncated)"
    return text + "\n\n===== TAIL =====\n" + "\n".join(tail)[-7000:]


def post(name: str, title: str, text: str) -> None:
    repo, sha, token = os.environ["GITHUB_REPOSITORY"], os.environ["HEAD_SHA"], os.environ["GITHUB_TOKEN"]
    body = {
        "name": name,
        "head_sha": sha,
        "status": "completed",
        "conclusion": "neutral",
        "output": {"title": title, "summary": title, "text": "```\n" + text[:LIMIT] + "\n```"},
    }
    req = urllib.request.Request(
        f"https://api.github.com/repos/{repo}/check-runs",
        data=json.dumps(body).encode(),
        headers={"Authorization": f"Bearer {token}", "Accept": "application/vnd.github+json", "Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req) as resp:
        print(name, resp.status)


def main() -> None:
    outcomes = dict(arg.split("=", 1) for arg in sys.argv[1:])
    summary = []
    for stage, outcome in outcomes.items():
        log = f"{stage}.log"
        text = condense(log) if os.path.exists(log) else "(no log)"
        title = f"{stage}: {outcome}"
        summary.append(f"### {title}\n\n```\n{text[-6000:]}\n```\n")
        try:
            post(f"report-{stage}", title, text)
        except Exception as exc:  # reporting must never fail the job
            print("could not publish check run:", exc)
    step_summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if step_summary:
        with open(step_summary, "a") as fh:
            fh.write("\n".join(summary))


if __name__ == "__main__":
    main()
