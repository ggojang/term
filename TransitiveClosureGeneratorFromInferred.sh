#!/bin/bash

#----------------------------------------------------------------
# SNOMED CT Transitive Closure (TC) 생성 스크립트 (PostgreSQL - 최적화판)
#
# 개선 사항:
#   - INFERRED_RELATIONSHIP (Full) 자기조인 대신 INFERRED_RELATIONSHIP_SNAP 직접 사용
#     (SNAP은 이미 UUID당 최신 1건으로 deduplicate 완료 → GROUP BY 자기조인 불필요)
#   - Step 1에서 DESCRIPTION_TP를 조인해 FSN을 처음부터 포함 → 별도 UPDATE 불필요
#   - 소요 시간: 수 시간+ → 수 분
#
# 전제 조건:
#   - INFERRED_RELATIONSHIP_SNAP 테이블에 (type_id, active) 인덱스 필요:
#     CREATE INDEX IF NOT EXISTS idx_inferred_snap_type_active
#       ON term.inferred_relationship_snap(type_id, active);
#----------------------------------------------------------------

CURRENT_DATE=$(date +"%Y%m%d_%H%M%S")

PG_HOST="127.0.0.1"
PG_PORT="5432"
PG_DB="term"
PG_USER="postgres"
PG_PASS="julab123!"
PG_SCHEMA="term"

ISA_REL="116680003"
FSN_TYPE="900000000000003001"

ISA_FILE="/tmp/ISA_${CURRENT_DATE}.tsv"
TC_FILE="/tmp/TC_${CURRENT_DATE}.tsv"

export PGPASSWORD="${PG_PASS}"
PSQL="psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB}"

echo "============================================"
echo " SNOMED CT TC 생성 시작 (PostgreSQL - 최적화판)"
echo " DB   : ${PG_HOST}:${PG_PORT}/${PG_DB}"
echo "============================================"

# ── 전제 인덱스 보장 ──────────────────────────────────────────
${PSQL} --no-psqlrc -c "
CREATE INDEX IF NOT EXISTS idx_inferred_snap_type_active
  ON ${PG_SCHEMA}.inferred_relationship_snap(type_id, active);" 2>/dev/null

# ── STEP 1: ISA 쌍 추출 + FSN 조인 (SNAP + DESCRIPTION_TP) ────
echo "[1/4] ISA 관계 추출 중 (FSN 포함)..."
T1=${SECONDS}

${PSQL} --no-psqlrc -t -A -F $'\t' -c \
  "SET search_path TO ${PG_SCHEMA}; \
   SELECT r.SOURCE_ID, r.DESTINATION_ID, \
     COALESCE(replace(replace(d.term, '\"', ''), E'\\t', ' '), r.SOURCE_ID::text) \
   FROM ${PG_SCHEMA}.INFERRED_RELATIONSHIP_SNAP r \
   LEFT JOIN ( \
       SELECT DISTINCT ON (concept_id) concept_id, term \
       FROM ${PG_SCHEMA}.DESCRIPTION_TP \
       WHERE type_id = '${FSN_TYPE}' AND active = 1 AND language_code = 'en' \
       ORDER BY concept_id, effective_time DESC \
   ) d ON d.concept_id = r.SOURCE_ID \
   WHERE r.TYPE_ID = '${ISA_REL}' AND r.ACTIVE = 1;" \
  | grep -v '^$' \
  > "${ISA_FILE}"

ROWS=$(wc -l < "${ISA_FILE}" | tr -d ' ')
echo "    → ${ROWS} 건, 소요: $((SECONDS-T1))초"

# ── STEP 2: TransitiveClosureGenerator 컴파일 ─────────────────
echo "[2/4] TransitiveClosureGenerator.java 컴파일 중..."
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
javac -encoding UTF-8 -d . "${SCRIPT_DIR}/TransitiveClosureGenerator.java"
if [ $? -ne 0 ]; then
    echo "컴파일 실패. 종료합니다."
    rm -f "${ISA_FILE}"
    exit 1
fi

# ── STEP 3: TC 파일 생성 (Java) ───────────────────────────────
echo "[3/4] TC 계산 중 (Java, 4g heap)..."
T3=${SECONDS}
java -Xmx4g -cp . co.infoclinic.term.snomedct.TransitiveClosureGenerator "${ISA_FILE}" "${TC_FILE}"
if [ $? -ne 0 ]; then
    echo "TC 생성 실패. 종료합니다."
    rm -f "${ISA_FILE}"
    exit 1
fi
TC_ROWS=$(wc -l < "${TC_FILE}" | tr -d ' ')
echo "    → TC ${TC_ROWS} 건, 소요: $((SECONDS-T3))초"

# ── STEP 4: TC 테이블 적재 ────────────────────────────────────
echo "[4/4] TC 테이블 적재 중..."
T4=${SECONDS}
TC_FILE_IN_CONTAINER="/tmp/$(basename ${TC_FILE})"
docker cp "${TC_FILE}" term-postgres:"${TC_FILE_IN_CONTAINER}"
${PSQL} --no-psqlrc -c "
TRUNCATE TABLE ${PG_SCHEMA}.TC;
COPY ${PG_SCHEMA}.TC (CONCEPT_ID, PARENT_ID, TERM, CHILDREN_COUNT, DESCENDANT_COUNT, DEPTH, PATH)
FROM '${TC_FILE_IN_CONTAINER}' WITH (FORMAT CSV, DELIMITER E'\t', NULL '', QUOTE '\"');"
if [ $? -ne 0 ]; then
    echo "TC 적재 실패."
    rm -f "${ISA_FILE}" "${TC_FILE}"
    exit 1
fi
docker exec term-postgres rm -f "${TC_FILE_IN_CONTAINER}"
echo "    → 완료, 소요: $((SECONDS-T4))초"

rm -f "${ISA_FILE}" "${TC_FILE}"
echo "============================================"
echo " TC 생성 완료! 총 소요: ${SECONDS}초"
echo "============================================"
${PSQL} --no-psqlrc -t -c "SELECT COUNT(*) FROM ${PG_SCHEMA}.TC;" 2>/dev/null
