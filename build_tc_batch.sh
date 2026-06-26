#!/bin/bash
# SNOMED CT TC 전체 백필 스크립트 (valid_from/valid_to diff 방식)
#
# 사용법:
#   ./build_tc_batch.sh             # DATES 배열의 모든 날짜 처리
#   ./build_tc_batch.sh 20261201    # 단일 날짜만 처리
#
# 반드시 오래된 날짜 → 최신 날짜 순서로 실행 (diff 방식)
# TC_META에 이미 등록된 날짜는 자동으로 건너뜁니다.

set -e

PG_HOST="127.0.0.1"
PG_PORT="5432"
PG_DB="term"
PG_USER="postgres"
PG_PASS="julab123!"
export PGPASSWORD="${PG_PASS}"

PSQL="/Library/PostgreSQL/18/bin/psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB}"
LOCATION="$(cd "$(dirname "$0")" && pwd)"
PG_JDBC_JAR=$(find ~/.m2/repository/org/postgresql -name "postgresql-*.jar" | sort | tail -1)
CLASS_DIR="/tmp/term_loader_classes"
LOADER_SRC="${LOCATION}/src/main/java/co/infoclinic/term/common/loader/TransitiveClosureLoader.java"
LOG_FILE="${LOCATION}/tc_batch.log"

source ~/.sdkman/bin/sdkman-init.sh 2>/dev/null || true

mkdir -p "${CLASS_DIR}"

echo "========================================" | tee -a "${LOG_FILE}"
echo "TC 배치 시작: $(date)" | tee -a "${LOG_FILE}"

# 컴파일
javac -encoding UTF-8 -cp "${PG_JDBC_JAR}" -d "${CLASS_DIR}" "${LOADER_SRC}"
echo "컴파일 완료" | tee -a "${LOG_FILE}"

# 처리 대상: 인수가 있으면 그 날짜만, 없으면 전체 (오래된 순)
if [ $# -gt 0 ]; then
  DATES=("$@")
else
  DATES=(
    20020131 20020731
    20030131 20030731
    20040131 20040731
    20050131 20050731
    20060131 20060731
    20070131 20070731
    20080131 20080731
    20090131 20090731
    20100131 20100731
    20110131 20110731
    20120131 20120731
    20130131 20130731
    20140131 20140731
    20150131 20150731
    20160131 20160731
    20170131 20170731
    20180131 20180731
    20190131 20190731
    20200131 20200731
    20210131 20210731
    20210930 20211031 20211130
    20220131 20220228 20220331 20220430 20220531 20220630
    20220731 20220831 20220930 20221031 20221130 20221231
    20230131 20230228 20230331 20230430 20230531 20230630
    20230731 20230901 20231001 20231101 20231201
    20240101 20240201 20240301 20240401 20240501 20240601
    20240701 20240801 20240901 20241001 20241101 20241201
    20250101 20250201 20250301 20250401 20250501 20250601
    20250701 20250801 20250901 20251001 20251101 20251201
    20260101 20260201 20260301 20260401 20260501 20260601
  )
fi

TOTAL=${#DATES[@]}
DONE=0
SKIPPED=0

for ET in "${DATES[@]}"; do
  EXISTING=$(${PSQL} -tAc "SELECT COUNT(*) FROM term.tc_meta WHERE effective_time = '${ET}'" 2>/dev/null || echo "0")
  if [ "${EXISTING}" -gt "0" ]; then
    echo "[SKIP] ${ET} — TC_META에 이미 등록됨" | tee -a "${LOG_FILE}"
    SKIPPED=$((SKIPPED+1))
    continue
  fi

  DONE=$((DONE+1))
  echo "" | tee -a "${LOG_FILE}"
  echo "=== [${DONE}/${TOTAL}] ${ET} 시작 ($(date '+%H:%M:%S')) ===" | tee -a "${LOG_FILE}"
  START=$(date +%s)

  java -Xmx6g -cp "${CLASS_DIR}:${PG_JDBC_JAR}" \
    co.infoclinic.term.common.loader.TransitiveClosureLoader "${ET}" 2>&1 | tee -a "${LOG_FILE}"
  # pipe 종료코드 확인 (tee가 성공해도 java 실패 시 중단)
  if [ "${PIPESTATUS[0]}" -ne 0 ]; then
    echo "[ERROR] ${ET} 적재 실패 — 배치 중단" | tee -a "${LOG_FILE}"
    exit 1
  fi

  echo "=== ${ET} 완료: $(($(date +%s) - START))초 ===" | tee -a "${LOG_FILE}"
done

echo "" | tee -a "${LOG_FILE}"
echo "완료: $(date) — 처리=${DONE}개, 건너뜀=${SKIPPED}개" | tee -a "${LOG_FILE}"
${PSQL} -c "SELECT effective_time, row_count FROM term.tc_meta ORDER BY effective_time;" | tee -a "${LOG_FILE}"
