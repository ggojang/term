#!/usr/bin/env python3
"""
약학정보원 ATC 코드 전체 수집 → term.hira_atc_master 적재

출처: https://health.kr/searchDrug/ATCcode.renewal.asp
API:  https://health.kr/searchDrug/ajax/ajax_atccode.asp
        ?select_mode={1|2|3|4|5}&req_word={상위코드}

select_mode별 반환 코드 길이:
  1 → 1자리  (req_word 빈값)
  2 → 3자리  (req_word 1자리)
  3 → 4자리  (req_word 3자리)
  4 → 5자리  (req_word 4자리)
  5 → 7자리  (req_word 5자리, vol 포함)
"""
from __future__ import annotations

import logging
import sys
import time
from typing import Optional

import psycopg2
import psycopg2.extras
import requests

API_URL  = "https://health.kr/searchDrug/ajax/ajax_atccode.asp"
DELAY    = 0.15   # 서버 부하 방지 (초)

DB_CONFIG = dict(
    host="localhost", port=5432, dbname="term",
    user="postgres", password="julab123!",
    options="-c search_path=term",
)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
log = logging.getLogger(__name__)

# select_mode: (현재 코드 길이) → 다음 mode
NEXT_MODE = {1: 2, 3: 3, 4: 4, 5: 5}
# 요청 시 req_word에 넣을 상위 코드 길이
REQ_LEN   = {1: 0, 2: 1, 3: 3, 4: 4, 5: 5}


def fetch(mode: int, req_word: str) -> list[dict]:
    resp = requests.get(
        API_URL,
        params={"select_mode": mode, "req_word": req_word},
        timeout=20,
    )
    resp.raise_for_status()
    return resp.json()


def collect_all() -> list[dict]:
    """전체 ATC 계층을 BFS로 수집, 중간노드 포함 모든 레코드 반환."""
    results: list[dict] = []
    # (mode, req_word) 큐
    queue: list[tuple[int, str]] = [(1, "")]

    while queue:
        mode, req_word = queue.pop(0)
        time.sleep(DELAY)
        try:
            items = fetch(mode, req_word)
        except Exception as e:
            log.warning("fetch 실패 mode=%s req=%s: %s", mode, req_word, e)
            continue

        for item in items:
            code     = item.get("atc_code", "").strip()
            atc_name = item.get("atc_name", "") or ""
            atc_hname= item.get("atc_hname", "") or ""
            vol      = item.get("vol") or None
            datatree = int(item.get("datatree") or 0)
            clen     = len(code)

            results.append({
                "atc_code":  code,
                "atc_name":  atc_name.strip(),
                "atc_hname": atc_hname.strip(),
                "vol":       vol,
            })

            # 하위가 있으면 큐에 추가
            if datatree > 0:
                next_mode = {1: 2, 3: 3, 4: 4, 5: None}.get(clen)
                if next_mode:
                    queue.append((next_mode, code))

    return results


def upsert(conn, records: list[dict]) -> int:
    sql = """
        INSERT INTO term.hira_atc_master (atc_code, atc_name, atc_hname, vol)
        VALUES %s
        ON CONFLICT (atc_code) DO UPDATE SET
            atc_name  = EXCLUDED.atc_name,
            atc_hname = EXCLUDED.atc_hname,
            vol       = EXCLUDED.vol
    """
    rows = [(r["atc_code"], r["atc_name"], r["atc_hname"], r["vol"]) for r in records]
    with conn.cursor() as cur:
        psycopg2.extras.execute_values(cur, sql, rows, page_size=500)
    conn.commit()
    return len(rows)


def ensure_table(conn):
    with conn.cursor() as cur:
        cur.execute("""
            CREATE TABLE IF NOT EXISTS term.hira_atc_master (
                atc_code  VARCHAR(10) PRIMARY KEY,
                atc_name  VARCHAR(255),
                atc_hname VARCHAR(255),
                vol       VARCHAR(100)
            )
        """)
    conn.commit()


def main() -> int:
    log.info("=== ATC 마스터 수집 시작 (health.kr) ===")

    log.info("전체 ATC 계층 수집 중...")
    records = collect_all()
    log.info("수집 완료: %d건", len(records))

    # atc_code 기준 중복 제거 (마지막 항목 우선)
    seen: dict[str, dict] = {}
    for r in records:
        seen[r["atc_code"]] = r
    records = list(seen.values())
    log.info("중복 제거 후: %d건", len(records))

    conn = psycopg2.connect(**DB_CONFIG)
    try:
        ensure_table(conn)
        n = upsert(conn, records)
        log.info("DB 적재 완료: %d건 upsert", n)
    finally:
        conn.close()

    log.info("=== 완료 ===")
    return 0


if __name__ == "__main__":
    sys.exit(main())
