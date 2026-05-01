#!/usr/bin/env python3
"""Generate HTML test report with coverage data."""

import json
import os
import sys
from datetime import datetime
from pathlib import Path


def load_coverage_data(coverage_json_path: str) -> dict:
    """Load coverage data from JSON file."""
    try:
        with open(coverage_json_path) as f:
            return json.load(f)
    except (FileNotFoundError, json.JSONDecodeError):
        return {}


def generate_html_report(
    test_results: dict,
    coverage_data: dict,
    output_path: str = "reports/test_report.html",
) -> None:
    """Generate a beautiful HTML test report."""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S UTC")
    repo = os.environ.get("GITHUB_REPOSITORY", "local/repo")
    branch = os.environ.get("GITHUB_REF_NAME", "local")
    commit = os.environ.get("GITHUB_SHA", "local")[:8]
    run_id = os.environ.get("GITHUB_RUN_ID", "0")
    workflow = os.environ.get("GITHUB_WORKFLOW", "Local Run")

    # Parse test results
    total = test_results.get("summary", {}).get("total", 0)
    passed = test_results.get("summary", {}).get("passed", 0)
    failed = test_results.get("summary", {}).get("failed", 0)
    errors = test_results.get("summary", {}).get("errors", 0)
    skipped = test_results.get("summary", {}).get("skipped", 0)
    duration = test_results.get("duration", 0)
    pass_rate = (passed / total * 100) if total > 0 else 0

    # Parse coverage
    coverage_pct = coverage_data.get("totals", {}).get("percent_covered", 0)
    if isinstance(coverage_pct, float):
        coverage_pct = round(coverage_pct, 1)

    # Build test rows
    tests = test_results.get("tests", [])
    test_rows = ""
    for t in tests:
        name = t.get("nodeid", "unknown")
        outcome = t.get("outcome", "unknown")
        call_duration = t.get("call", {}).get("duration", 0) if t.get("call") else 0
        status_class = {
            "passed": "status-passed",
            "failed": "status-failed",
            "error": "status-error",
            "skipped": "status-skipped",
        }.get(outcome, "status-unknown")
        status_icon = {
            "passed": "✅",
            "failed": "❌",
            "error": "⚠️",
            "skipped": "⏭️",
        }.get(outcome, "❓")

        longrepr = ""
        if outcome in ("failed", "error") and t.get("call", {}).get("longrepr"):
            longrepr = f'<div class="error-detail"><pre>{t["call"]["longrepr"]}</pre></div>'

        test_rows += f"""
        <tr class="{status_class}-row">
          <td class="test-name">{name}</td>
          <td><span class="{status_class}">{status_icon} {outcome.upper()}</span></td>
          <td class="duration">{call_duration:.3f}s</td>
        </tr>
        {"<tr><td colspan='3'>" + longrepr + "</td></tr>" if longrepr else ""}
        """

    # Coverage file rows
    cov_rows = ""
    files = coverage_data.get("files", {})
    for filepath, data in files.items():
        pct = data.get("summary", {}).get("percent_covered", 0)
        if isinstance(pct, float):
            pct = round(pct, 1)
        stmts = data.get("summary", {}).get("num_statements", 0)
        missing = data.get("summary", {}).get("missing_lines", 0)
        bar_color = "#22c55e" if pct >= 80 else "#f59e0b" if pct >= 60 else "#ef4444"
        cov_rows += f"""
        <tr>
          <td class="file-name">{filepath}</td>
          <td>{stmts}</td>
          <td>{missing}</td>
          <td>
            <div class="coverage-bar-wrap">
              <div class="coverage-bar" style="width:{pct}%;background:{bar_color}"></div>
              <span>{pct}%</span>
            </div>
          </td>
        </tr>
        """

    pass_color = "#22c55e" if pass_rate >= 90 else "#f59e0b" if pass_rate >= 70 else "#ef4444"
    cov_color = "#22c55e" if coverage_pct >= 80 else "#f59e0b" if coverage_pct >= 60 else "#ef4444"

    html = f"""<!DOCTYPE html>
<html lang="uk">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Test Report — {repo}</title>
  <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;700&family=Syne:wght@400;600;800&display=swap" rel="stylesheet">
  <style>
    :root {{
      --bg: #0d0f14;
      --surface: #151820;
      --surface2: #1c2030;
      --border: #2a2f45;
      --text: #e2e8f0;
      --muted: #64748b;
      --accent: #6366f1;
      --green: #22c55e;
      --red: #ef4444;
      --yellow: #f59e0b;
      --blue: #38bdf8;
    }}
    * {{ margin: 0; padding: 0; box-sizing: border-box; }}
    body {{ background: var(--bg); color: var(--text); font-family: 'Syne', sans-serif; min-height: 100vh; }}
    .hero {{
      background: linear-gradient(135deg, #0d0f14 0%, #151c3a 50%, #0d0f14 100%);
      border-bottom: 1px solid var(--border);
      padding: 48px 40px 40px;
      position: relative;
      overflow: hidden;
    }}
    .hero::before {{
      content: '';
      position: absolute;
      top: -50%;
      left: -20%;
      width: 60%;
      height: 200%;
      background: radial-gradient(ellipse, rgba(99,102,241,0.08) 0%, transparent 70%);
      pointer-events: none;
    }}
    .hero-top {{ display: flex; align-items: flex-start; justify-content: space-between; flex-wrap: wrap; gap: 16px; }}
    .hero-title {{ font-size: 2rem; font-weight: 800; color: #fff; letter-spacing: -0.03em; }}
    .hero-title span {{ color: var(--accent); }}
    .hero-meta {{ font-family: 'JetBrains Mono', monospace; font-size: 0.78rem; color: var(--muted); margin-top: 8px; display: flex; flex-wrap: wrap; gap: 16px; }}
    .badge {{ display: inline-flex; align-items: center; gap: 6px; background: var(--surface2); border: 1px solid var(--border); border-radius: 6px; padding: 4px 10px; font-size: 0.75rem; font-family: 'JetBrains Mono', monospace; }}
    .badge-accent {{ border-color: var(--accent); color: var(--accent); }}
    .stats-grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; padding: 32px 40px; }}
    .stat-card {{
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 24px;
      position: relative;
      overflow: hidden;
      transition: border-color 0.2s;
    }}
    .stat-card:hover {{ border-color: var(--accent); }}
    .stat-label {{ font-size: 0.75rem; color: var(--muted); text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 10px; }}
    .stat-value {{ font-size: 2.4rem; font-weight: 800; font-family: 'JetBrains Mono', monospace; }}
    .stat-sub {{ font-size: 0.78rem; color: var(--muted); margin-top: 6px; }}
    .ring-wrap {{ position: absolute; right: 16px; top: 50%; transform: translateY(-50%); opacity: 0.15; font-size: 3rem; }}
    .section {{ padding: 0 40px 40px; }}
    .section-title {{ font-size: 1.1rem; font-weight: 700; color: var(--text); margin-bottom: 16px; display: flex; align-items: center; gap: 10px; }}
    .section-title::after {{ content: ''; flex: 1; height: 1px; background: var(--border); }}
    table {{ width: 100%; border-collapse: collapse; background: var(--surface); border: 1px solid var(--border); border-radius: 12px; overflow: hidden; font-size: 0.85rem; }}
    th {{ background: var(--surface2); padding: 12px 16px; text-align: left; font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.08em; color: var(--muted); border-bottom: 1px solid var(--border); }}
    td {{ padding: 11px 16px; border-bottom: 1px solid rgba(42,47,69,0.5); }}
    tr:last-child td {{ border-bottom: none; }}
    tr:hover td {{ background: rgba(99,102,241,0.04); }}
    .test-name {{ font-family: 'JetBrains Mono', monospace; font-size: 0.8rem; color: #a5b4fc; }}
    .file-name {{ font-family: 'JetBrains Mono', monospace; font-size: 0.8rem; color: var(--blue); }}
    .duration {{ font-family: 'JetBrains Mono', monospace; color: var(--muted); font-size: 0.8rem; }}
    .status-passed {{ color: var(--green); font-weight: 700; font-size: 0.78rem; }}
    .status-failed {{ color: var(--red); font-weight: 700; font-size: 0.78rem; }}
    .status-error {{ color: var(--yellow); font-weight: 700; font-size: 0.78rem; }}
    .status-skipped {{ color: var(--muted); font-weight: 700; font-size: 0.78rem; }}
    .status-passed-row td {{ border-left: 3px solid var(--green); }}
    .status-failed-row td {{ border-left: 3px solid var(--red); background: rgba(239,68,68,0.03); }}
    .coverage-bar-wrap {{ display: flex; align-items: center; gap: 10px; }}
    .coverage-bar {{ height: 6px; border-radius: 3px; transition: width 0.3s; min-width: 2px; }}
    .error-detail pre {{ background: rgba(239,68,68,0.08); border: 1px solid rgba(239,68,68,0.2); border-radius: 8px; padding: 12px; font-family: 'JetBrains Mono', monospace; font-size: 0.75rem; color: #fca5a5; white-space: pre-wrap; word-break: break-all; }}
    .footer {{ text-align: center; padding: 24px; border-top: 1px solid var(--border); color: var(--muted); font-size: 0.8rem; font-family: 'JetBrains Mono', monospace; }}
  </style>
</head>
<body>

<div class="hero">
  <div class="hero-top">
    <div>
      <div class="hero-title">🧪 Test <span>Report</span></div>
      <div class="hero-meta">
        <span>📦 {repo}</span>
        <span>🌿 {branch}</span>
        <span>🔖 {commit}</span>
        <span>⏱ {timestamp}</span>
        <span>🔄 Run #{run_id}</span>
      </div>
    </div>
    <div style="display:flex;flex-direction:column;gap:8px;align-items:flex-end;">
      <span class="badge badge-accent">⚡ {workflow}</span>
      <span class="badge">⏱ {duration:.2f}s total</span>
    </div>
  </div>
</div>

<div class="stats-grid">
  <div class="stat-card">
    <div class="stat-label">Total Tests</div>
    <div class="stat-value" style="color:var(--blue)">{total}</div>
    <div class="stat-sub">All test cases</div>
    <div class="ring-wrap">🧪</div>
  </div>
  <div class="stat-card">
    <div class="stat-label">Passed</div>
    <div class="stat-value" style="color:var(--green)">{passed}</div>
    <div class="stat-sub">Success</div>
    <div class="ring-wrap">✅</div>
  </div>
  <div class="stat-card">
    <div class="stat-label">Failed</div>
    <div class="stat-value" style="color:var(--red)">{failed}</div>
    <div class="stat-sub">Need attention</div>
    <div class="ring-wrap">❌</div>
  </div>
  <div class="stat-card">
    <div class="stat-label">Pass Rate</div>
    <div class="stat-value" style="color:{pass_color}">{pass_rate:.1f}%</div>
    <div class="stat-sub">Success ratio</div>
    <div class="ring-wrap">📊</div>
  </div>
  <div class="stat-card">
    <div class="stat-label">Coverage</div>
    <div class="stat-value" style="color:{cov_color}">{coverage_pct}%</div>
    <div class="stat-sub">Code covered</div>
    <div class="ring-wrap">🎯</div>
  </div>
</div>

<div class="section">
  <div class="section-title">📋 Test Results ({total} tests)</div>
  <table>
    <thead>
      <tr><th>Test ID</th><th>Status</th><th>Duration</th></tr>
    </thead>
    <tbody>
      {test_rows if test_rows else '<tr><td colspan="3" style="text-align:center;color:var(--muted);padding:24px">No test data available</td></tr>'}
    </tbody>
  </table>
</div>

{"<div class='section'><div class='section-title'>📁 Coverage by File</div><table><thead><tr><th>File</th><th>Statements</th><th>Missing</th><th>Coverage</th></tr></thead><tbody>" + cov_rows + "</tbody></table></div>" if cov_rows else ""}

<div class="footer">
  Generated by GitHub Actions · {timestamp} · {repo}
</div>

</body>
</html>"""

    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(html)
    print(f"✅ HTML report generated: {output_path}")


if __name__ == "__main__":
    json_path = sys.argv[1] if len(sys.argv) > 1 else "reports/test_results.json"
    coverage_path = sys.argv[2] if len(sys.argv) > 2 else "reports/coverage.json"
    output = sys.argv[3] if len(sys.argv) > 3 else "reports/test_report.html"

    test_data = load_coverage_data(json_path)
    cov_data = load_coverage_data(coverage_path)

    generate_html_report(test_data, cov_data, output)
