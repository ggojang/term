#!/bin/bash
# SNOMED CT Semi-annual 릴리즈 TC 배치 생성 스크립트
# 이미 TC가 있는 날짜는 건너뜁니다.

set -e

PG_HOST="127.0.0.1"
PG_PORT="5432"
PG_DB="term"
PG_USER="postgres"
PG_PASS="julab123!"
export PGPASSWORD="${PG_PASS}"

PSQL="/Library/PostgreSQL/18/bin/psql"
LOCATION="$(cd "$(dirname "$0")" && pwd)"
PG_JDBC_JAR=$(find ~/.m2/repository/org/postgresql -name "postgresql-*.jar" | sort | tail -1)
CLASS_DIR="/tmp/term_loader_classes"
LOADER_SRC="${LOCATION}/src/main/java/co/infoclinic/term/common/loader/TransitiveClosureLoader.java"
LOG_FILE="${LOCATION}/tc_batch.log"

source ~/.sdkman/bin/sdkman-init.sh 2>/dev/null || true

mkdir -p "${CLASS_DIR}"

echo "========================================" | tee -a "${LOG_FILE}"
echo "TC 배치 생성 시작: $(date)" | tee -a "${LOG_FILE}"
echo "JDBC: ${PG_JDBC_JAR}" | tee -a "${LOG_FILE}"

# 컴파일
echo "TransitiveClosureLoader 컴파일 중..." | tee -a "${LOG_FILE}"
javac -encoding UTF-8 -cp "${PG_JDBC_JAR}" -d "${CLASS_DIR}" "${LOADER_SRC}"
echo "컴파일 완료" | tee -a "${LOG_FILE}"

# Semi-annual 대상 날짜 (최신→과거 순)
DATES=(
  20260101
  20250701
  20240701 20240101
  20230731 20230131
  20220731 20220131
  20210731 20210131
  20200731 20200131
  20190731 20190131
  20180731 20180131
  20170731 20170131
  20160731 20160131
  20150731 20150131
  20140731 20140131
  20130731 20130131
  20120731 20120131
  20110731 20110131
  20100131
  20090131
  20080131
  20070131
  20060131
  20050131
  20040131
  20030131
  20020131
)

TOTAL=${#DATES[@]}
DONE=0
SKIPPED=0

for ET in "${DATES[@]}"; do
  # 이미 TC가 있으면 건너뜀
  EXISTING=$(${PSQL} -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB} \
    -tAc "SELECT COUNT(*) FROM term.tc WHERE effective_time = '${ET}'" 2>/dev/null || echo "0")
  
  if [ "${EXISTING}" -gt "0" ]; then
    echo "[SKIP] ${ET} — 이미 ${EXISTING}건 존재" | tee -a "${LOG_FILE}"
    SKIPPED=$((SKIPPED+1))
    continue
  fi

  DONE=$((DONE+1))
  REMAIN=$((TOTAL - DONE - SKIPPED))
  echo "" | tee -a "${LOG_FILE}"
  echo "=== [${DONE}/${TOTAL}] ${ET} TC 생성 시작 ($(date '+%H:%M:%S')) ===" | tee -a "${LOG_FILE}"
  START=$(date +%s)

  java -Xmx6g -cp "${CLASS_DIR}:${PG_JDBC_JAR}" \
    co.infoclinic.term.common.loader.TransitiveClosureLoader "${ET}" 2>&1 | tee -a "${LOG_FILE}"

  END=$(date +%s)
  ELAPSED=$((END - START))
  echo "=== ${ET} 완료: ${ELAPSED}초 ===" | tee -a "${LOG_FILE}"

  # TC_META 갱신 (배치 단위로 즉시 반영)
  ${PSQL} -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB} -c \
    "INSERT INTO term.TC_META (EFFECTIVE_TIME, ROW_COUNT)
     SELECT EFFECTIVE_TIME, COUNT(*) FROM term.TC WHERE EFFECTIVE_TIME = '${ET}' GROUP BY EFFECTIVE_TIME
     ON CONFLICT (EFFECTIVE_TIME) DO UPDATE SET ROW_COUNT = EXCLUDED.ROW_COUNT;" \
    >> "${LOG_FILE}" 2>&1
  echo "    TC_META 갱신 완료 (${ET})" | tee -a "${LOG_FILE}"
done

echo "" | tee -a "${LOG_FILE}"
echo "========================================" | tee -a "${LOG_FILE}"
echo "전체 완료: $(date)" | tee -a "${LOG_FILE}"
echo "생성: $((TOTAL - SKIPPED))개, 건너뜀: ${SKIPPED}개" | tee -a "${LOG_FILE}"

# 완료 후 TC 현황
echo "" | tee -a "${LOG_FILE}"
${PSQL} -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB} \
  -c "SELECT effective_time, COUNT(*) rows FROM term.tc GROUP BY effective_time ORDER BY effective_time DESC;" \
  | tee -a "${LOG_FILE}"
