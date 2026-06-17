#!/usr/bin/env python3
"""
HIRA 청구관련기준(마스터파일) 자동 다운로드 스크립트

게시판: https://biz.hira.or.kr  →  심사기준 종합서비스  →  청구관련기준(마스터파일)
Board ID: BBSMSTR_000000000676

처리 흐름:
  1) selectComBbsList.ndo  → 최신 게시글 목록 (SSV 포맷)
  2) 제목 앞 [행위]/[약제]/[치료재료] 로 카테고리 분류, 최신 게시글 선택
  3) selectComBbsFileList.ndo → 첨부파일 목록 (100KB 초과 엑셀만)
  4) /com/dext5handler.ndo  → 파일 다운로드
  5) PostgreSQL hira_downloads 테이블에 이력 기록
  6) Slack 알림

스케줄: cron 매일 09:00
"""
from __future__ import annotations

import hashlib
import logging
import os
import sys
import urllib.parse
from datetime import date, datetime
from pathlib import Path

import psycopg2
import requests

# ── 설정 ──────────────────────────────────────────────────────────────────────
BASE_URL = "https://biz.hira.or.kr"
BBS_ID   = "BBSMSTR_000000000676"
REFERER  = f"{BASE_URL}/popup.ndo?formname=qya_bizcom%3A%3AInfoBank.xfdl&framename=InfoBank"

DOWNLOAD_DIR      = Path(__file__).parent.parent / "release_files" / "hira_incoming"
SLACK_WEBHOOK_URL = os.environ.get("HIRA_SLACK_WEBHOOK", "")
MIN_FILE_SIZE     = 100 * 1024  # 100 KB

DB_CONFIG = {
    "host":    "localhost",
    "port":    5432,
    "dbname":  "term",
    "user":    "postgres",
    "password":"julab123!",
    "options": "-c search_path=term",
}

CATEGORIES = ["행위", "약제", "치료재료"]

# SSV 구분자
RS  = "\x1e"
US  = "\x1f"
NUL = "\x03"

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler(Path(__file__).parent / "hira_download.log", encoding="utf-8"),
    ],
)
log = logging.getLogger(__name__)


# ── HTTP 헬퍼 ─────────────────────────────────────────────────────────────────
def _session() -> requests.Session:
    s = requests.Session()
    s.headers.update({
        "User-Agent": "Mozilla/5.0",
        "Referer":    REFERER,
    })
    return s


def parse_ssv(text: str) -> dict[str, list[dict]]:
    datasets: dict[str, list[dict]] = {}
    parts = text.split(RS)
    i = 0
    while i < len(parts):
        p = parts[i]
        if p.startswith("Dataset:"):
            ds_name = p[8:]
            cols = [c.split(":")[0] for c in parts[i + 1].split(US)]
            rows: list[dict] = []
            i += 2
            while i < len(parts) and not parts[i].startswith("Dataset:") and parts[i]:
                fields = parts[i].split(US)
                if fields[0] in ("N", "U", "I", "D"):
                    rows.append({cols[j]: (fields[j] if j < len(fields) else "") for j in range(len(cols))})
                i += 1
            datasets[ds_name] = rows
        else:
            i += 1
    return datasets


def build_list_body(bbs_id: str, page: int = 1, per_page: int = 20) -> bytes:
    first = (page - 1) * per_page
    last  = page * per_page
    return (
        f"SSV:utf-8{RS}JSESSIONID=null{RS}BIZINTERSESSION={RS}WMONID=5lnFrOSL_fb{RS}"
        f"browserType=Chrome{RS}osVersion=Mac OS 10.15.7{RS}"
        f"navigatorName=Chrome{RS}navigatorVersion=148{RS}"
        f"Dataset:dsParam{RS}"
        f"_RowType_{US}brdTyBltNo:STRING(256){US}bltNo:STRING(256){US}totCnt:STRING(256){US}"
        f"currentPage:STRING(256){US}recordCountPerPage:STRING(256){US}"
        f"firstIndex:STRING(256){US}lastIndex:STRING(256){US}"
        f"bbsId:STRING(256){US}cbSearchCnd:STRING(256){US}edSearchWrd:STRING(256){US}"
        f"nttId:STRING(256){US}atchFileId:STRING(256){US}codeId:STRING(256){US}"
        f"catType01Val:STRING(256){US}catType02Val:STRING(256){US}catType03Val:STRING(256){RS}"
        f"N{US}{US}{US}{US}{page}{US}{per_page}{US}{first}{US}{last}{US}"
        f"{bbs_id}{US}all{US}{NUL}{US}{US}{US}{US}{US}{US}{RS}{RS}"
    ).encode("utf-8")


def build_filelist_body(ntt_id: str, atch_file_id: str) -> bytes:
    return (
        f"SSV:utf-8{RS}JSESSIONID=null{RS}BIZINTERSESSION={RS}WMONID=5lnFrOSL_fb{RS}"
        f"browserType=Chrome{RS}osVersion=Mac OS 10.15.7{RS}"
        f"navigatorName=Chrome{RS}navigatorVersion=148{RS}"
        f"Dataset:dsParam{RS}"
        f"_RowType_{US}atchFileId:STRING(256){US}nttId:STRING(256){RS}"
        f"N{US}{atch_file_id}{US}{ntt_id}{RS}{RS}"
    ).encode("utf-8")


# ── DB ────────────────────────────────────────────────────────────────────────
def get_db():
    return psycopg2.connect(**DB_CONFIG)


def is_downloaded(conn, post_no: str) -> bool:
    with conn.cursor() as cur:
        cur.execute("SELECT 1 FROM hira_downloads WHERE post_no = %s", (post_no,))
        return cur.fetchone() is not None


def record_download(conn, *, category, file_type, post_no, title,
                    post_date, filename, saved_path, file_hash):
    with conn.cursor() as cur:
        cur.execute(
            "INSERT INTO hira_downloads"
            " (category, file_type, post_no, title, post_date, filename, saved_path, file_hash)"
            " VALUES (%s,%s,%s,%s,%s,%s,%s,%s)"
            " ON CONFLICT (post_no) DO NOTHING",
            (category, file_type, post_no, title, post_date,
             filename, str(saved_path), file_hash),
        )
    conn.commit()


# ── 게시글 목록 수집 ──────────────────────────────────────────────────────────
def parse_post_date(row: dict) -> date | None:
    """frstregisterPntt (yyyyMMddHHmmssSSS) → date."""
    raw = row.get("frstregisterPntt", "")
    if raw and len(raw) >= 8:
        try:
            return datetime.strptime(raw[:8], "%Y%m%d").date()
        except ValueError:
            pass
    return None


def fetch_latest_posts(sess: requests.Session) -> list[dict]:
    """
    카테고리별로 100KB 이상 엑셀파일이 있는 가장 최신 게시글 반환.
    제목 형식: [행위]/[약제]/[치료재료] 로 시작하는 글만 수집.
    """
    found: dict[str, dict] = {}  # category → best post

    for page in range(1, 10):
        body = build_list_body(BBS_ID, page=page, per_page=20)
        r = sess.post(
            f"{BASE_URL}/qya/bbs/selectComBbsList.ndo",
            data=body,
            headers={"Content-Type": "text/plain;charset=UTF-8"},
            timeout=20,
        )
        r.raise_for_status()
        ds = parse_ssv(r.text)
        rows = ds.get("dsMain", [])
        if not rows:
            break

        stop = False
        for row in rows:
            title = row.get("nttSj", "")
            cat = None
            for c in CATEGORIES:
                if title.startswith(f"[{c}]"):
                    cat = c
                    break
            if not cat or cat in found:
                continue

            post_date = parse_post_date(row)
            if post_date and post_date.year < 2024:
                stop = True
                break

            atch_file_id = row.get("atchFileId", "")
            if not atch_file_id:
                continue

            # 첨부파일 목록 확인
            ntt_id = row.get("nttId", "")
            post_info = {
                "category":    cat,
                "title":       title,
                "ntt_id":      ntt_id,
                "atch_file_id":atch_file_id,
                "post_no":     ntt_id,
                "post_date":   post_date,
            }
            files = fetch_file_list(sess, post_info)
            has_big_excel = any(
                int(f.get("apndFileSz", "0") or "0") >= MIN_FILE_SIZE
                and Path(f.get("apndFileNm", "")).suffix.lower() in (".xlsx", ".xlsb", ".xls", ".zip")
                for f in files
            )
            if has_big_excel:
                found[cat] = post_info
                log.info("마스터 게시글: [%s] %s (%s)", cat, title[:60], post_date)

        if stop or len(found) == len(CATEGORIES):
            break

    return list(found.values())


# ── 첨부파일 목록 수집 ────────────────────────────────────────────────────────
def fetch_file_list(sess: requests.Session, post: dict) -> list[dict]:
    body = build_filelist_body(post["ntt_id"], post["atch_file_id"])
    r = sess.post(
        f"{BASE_URL}/qya/bbs/selectComBbsFileList.ndo",
        data=body,
        headers={"Content-Type": "text/plain;charset=UTF-8"},
        timeout=20,
    )
    r.raise_for_status()
    ds = parse_ssv(r.text)
    files = ds.get("dsFileList01", [])
    log.info("  첨부파일 %d개", len(files))
    return files


# ── 파일 다운로드 ──────────────────────────────────────────────────────────────
def download_file(sess: requests.Session, apnd_file_id: str,
                  apnd_file_stg_pth: str, apnd_file_nm: str,
                  save_path: Path) -> str:
    """파일을 다운로드하고 SHA-256 해시를 반환."""
    virtual_path = apnd_file_stg_pth.rstrip("/") + "/" + apnd_file_id
    url = (
        f"{BASE_URL}/com/dext5handler.ndo"
        f"?dext5CMD=downloadRequest&resumeMode=0&fileNameRuleEx=0"
        f"&fileVirtualPath={urllib.parse.quote(virtual_path)}"
        f"&fileOrgName={urllib.parse.quote(apnd_file_nm)}"
    )
    r = sess.get(url, timeout=120, stream=True)
    r.raise_for_status()

    sha256 = hashlib.sha256()
    with open(save_path, "wb") as f:
        for chunk in r.iter_content(65536):
            f.write(chunk)
            sha256.update(chunk)
    return sha256.hexdigest()


# ── Slack 알림 ────────────────────────────────────────────────────────────────
def send_slack(new_files: list[dict]):
    if not SLACK_WEBHOOK_URL or not new_files:
        if not SLACK_WEBHOOK_URL:
            log.info("Slack webhook 미설정, 알림 생략")
        return
    lines = [f"*HIRA 코드자료 신규 다운로드 ({date.today()})* — {len(new_files)}건\n"]
    for f in new_files:
        lines.append(
            f"• [{f['category']}] {f['title'][:50]} ({f['post_date']}) → `{f['filename']}`"
        )
    try:
        requests.post(SLACK_WEBHOOK_URL, json={"text": "\n".join(lines)}, timeout=10).raise_for_status()
        log.info("Slack 알림 완료")
    except Exception as e:
        log.error("Slack 알림 실패: %s", e)


# ── 메인 ──────────────────────────────────────────────────────────────────────
def main() -> int:
    log.info("=== HIRA 다운로드 시작 ===")
    DOWNLOAD_DIR.mkdir(parents=True, exist_ok=True)

    conn = get_db()
    sess = _session()
    all_new: list[dict] = []

    try:
        posts = fetch_latest_posts(sess)
        log.info("카테고리별 최신 게시글: %d건", len(posts))

        for post in posts:
            cat      = post["category"]
            post_no  = post["post_no"]
            title    = post["title"]
            post_date= post["post_date"]

            if is_downloaded(conn, post_no):
                log.info("[%s] 이미 다운로드됨: %s", cat, title[:50])
                continue

            files = fetch_file_list(sess, post)
            downloaded_any = False

            for finfo in files:
                size_str = finfo.get("apndFileSz", "0")
                size = int(size_str) if size_str.isdigit() else 0
                if size < MIN_FILE_SIZE:
                    log.info("  건너뜀 (크기 %d B < 100KB): %s", size, finfo.get("apndFileNm",""))
                    continue

                apnd_file_id  = finfo.get("apndFileId", "")
                apnd_file_stg = finfo.get("apndFileStgPth", "/share/internet/HIRA_BBS_FILE/upload/")
                apnd_file_nm  = finfo.get("apndFileNm", f"hira_{post_no}.bin")
                file_sn       = finfo.get("fileSn", "0")

                ext = Path(apnd_file_nm).suffix.lower()
                if ext not in (".xlsx", ".xlsb", ".xls", ".zip"):
                    log.info("  건너뜀 (엑셀/ZIP 아님): %s", apnd_file_nm)
                    continue

                # 고유 post_no: 파일이 여러 개면 fileSn 추가
                file_post_no = f"{post_no}_{file_sn}" if file_sn != "0" else post_no
                if is_downloaded(conn, file_post_no):
                    log.info("  이미 다운로드됨: %s", apnd_file_nm)
                    continue

                save_path = DOWNLOAD_DIR / apnd_file_nm
                log.info("  다운로드 중 (%d MB): %s", size // (1024*1024), apnd_file_nm)

                try:
                    file_hash = download_file(sess, apnd_file_id, apnd_file_stg, apnd_file_nm, save_path)
                    log.info("  저장 완료: %s  sha256=%s…", save_path.name, file_hash[:12])

                    record_download(
                        conn,
                        category=cat, file_type="마스터",
                        post_no=file_post_no, title=title,
                        post_date=post_date, filename=save_path.name,
                        saved_path=save_path, file_hash=file_hash,
                    )
                    all_new.append({**post, "filename": save_path.name})
                    downloaded_any = True

                except Exception as e:
                    log.error("  다운로드 실패: %s — %s", apnd_file_nm, e)

            # 파일이 없어도 게시글 자체를 기록 (재시도 방지)
            if not downloaded_any and not is_downloaded(conn, post_no):
                record_download(
                    conn,
                    category=cat, file_type="마스터",
                    post_no=post_no, title=title,
                    post_date=post_date, filename=None,
                    saved_path=None, file_hash=None,
                )

    finally:
        conn.close()

    send_slack(all_new)
    log.info("=== 완료: 신규 파일 %d건 ===", len(all_new))
    return 0


if __name__ == "__main__":
    sys.exit(main())
