#!/usr/bin/env python3
"""KPIS KD코드 표준코드목록 (StdCdList.csv) → term.kdcode 적재 스크립트

사용법:
  python3 load_kdcode.py [StdCdList.csv 경로]
  경로 생략 시 release_files/hira_incoming/StdCdList.csv 사용
"""
from __future__ import annotations
import csv
import logging
import sys
import time
from pathlib import Path

import psycopg2

DEFAULT_CSV = Path(__file__).parent.parent / "release_files" / "hira_incoming" / "StdCdList.csv"
DB_CONFIG = dict(host="localhost", port=5432, dbname="term", user="postgres",
                 password="julab123!", options="-c search_path=term")

logging.basicConfig(level=logging.INFO,
                    format="%(asctime)s [%(levelname)s] %(message)s",
                    handlers=[logging.StreamHandler(sys.stdout)])
log = logging.getLogger(__name__)

INSERT_SQL = """
INSERT INTO term.kdcode (
    표준코드, 표준코드명칭, 적용개시일자, 적용종료일자,
    양도개시일자, 양도종료일자, 포장내제품총수량, 상한가,
    급여비급여구분, 안전상비의약품여부, 일반전문구분, 퇴장방지저가방사선,
    품목기준코드, 식약처취소일자, 일련번호제외여부, 일련번호제외사유, 의약품판독장비구분
) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
"""


def load(csv_path: Path) -> None:
    log.info("CSV: %s", csv_path)
    conn = psycopg2.connect(**DB_CONFIG)
    conn.autocommit = False
    cur = conn.cursor()

    cur.execute("TRUNCATE term.kdcode")
    conn.commit()
    log.info("term.kdcode truncated")

    start = time.time()
    batch: list[tuple] = []
    count = 0
    skipped = 0

    with open(csv_path, encoding="utf-8-sig", newline="") as f:
        reader = csv.reader(f)
        for i, row in enumerate(reader):
            if i == 0:
                log.info("메타데이터 스킵: %s", row)
                continue
            if len(row) != 17:
                skipped += 1
                continue
            batch.append(tuple(v if v != "" else None for v in row))
            if len(batch) >= 5000:
                cur.executemany(INSERT_SQL, batch)
                conn.commit()
                count += len(batch)
                batch = []
                if count % 100_000 == 0:
                    log.info("%d건 적재... (%.1fs)", count, time.time() - start)

    if batch:
        cur.executemany(INSERT_SQL, batch)
        conn.commit()
        count += len(batch)

    log.info("완료: %d건 (skipped=%d, %.1fs)", count, skipped, time.time() - start)
    cur.close()
    conn.close()


if __name__ == "__main__":
    path = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_CSV
    if not path.exists():
        log.error("파일 없음: %s", path)
        sys.exit(1)
    load(path)
