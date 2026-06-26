#!/bin/bash
#----------------------------------------------------------------
# SNOMED CT Extension / Refset 단독 적재 스크립트
#
# International RF2는 이미 적재된 상태에서 Extension 또는 Refset만
# 추가할 때 사용합니다. (스키마 재생성 없이 append 모드로 적재)
#
# 사용법:
#   ./ExtensionImporter.sh <extension.zip> [extension.zip ...]
#
# 예시:
#   ./ExtensionImporter.sh release_files/SnomedCT_KoreanEdition_PRODUCTION_20260601T120000Z.zip
#   ./ExtensionImporter.sh release_files/ext1.zip release_files/ext2.zip
#
# 처리 순서:
#   1. RF2 데이터 append 적재 (concept, description, relationship 등)
#   2. REFERENCESET_ACTIVE 갱신
#   3. SCHEME 테이블 등록 (드롭박스에 표시)
#----------------------------------------------------------------

set -e

LOCATION="$(cd "$(dirname "$0")" && pwd)"

PG_HOST="127.0.0.1"
PG_PORT="5432"
PG_DB="term"
PG_USER="postgres"
PG_PASS="julab123!"

export PGPASSWORD="${PG_PASS}"
export JAVA_HOME=/usr/lib/jvm/java-17-oracle
export PATH=$HOME/bin:$JAVA_HOME/bin:$PATH

if [ $# -eq 0 ]; then
    echo "사용법: $0 <extension.zip> [extension.zip ...]"
    exit 1
fi

# JDBC JAR
PG_JDBC_JAR=$(find ~/.m2/repository/org/postgresql -name "postgresql-*.jar" 2>/dev/null | sort | tail -1)
if [ -z "${PG_JDBC_JAR}" ]; then
    PG_JDBC_JAR="${LOCATION}/postgresql.jar"
fi
if [ ! -f "${PG_JDBC_JAR}" ]; then
    echo "PostgreSQL JDBC JAR를 찾을 수 없습니다."
    exit 1
fi

LOADER_DIR="${LOCATION}/src/main/java/co/infoclinic/term/common/loader"
CLASS_DIR="/tmp/term_loader_classes"
mkdir -p "${CLASS_DIR}"

PSQL="psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB}"

echo "============================================================"
echo " SNOMED CT Extension 적재"
echo " DB: ${PG_HOST}:${PG_PORT}/${PG_DB}"
echo "============================================================"

# SnomedRf2Loader 컴파일
echo "SnomedRf2Loader 컴파일 중..."
javac -encoding UTF-8 -cp "${PG_JDBC_JAR}" -d "${CLASS_DIR}" \
    "${LOADER_DIR}/SnomedRf2Loader.java"
echo "컴파일 완료"

for ZIP in "$@"; do
    if [ ! -f "${ZIP}" ]; then
        echo "[SKIP] 파일 없음: ${ZIP}"
        continue
    fi

    BASENAME=$(basename "${ZIP}")
    EXT_ET=$(echo "${BASENAME}" | grep -oE '[0-9]{8}' | head -1)
    EXT_NAME=$(echo "${BASENAME}" \
        | sed 's/SnomedCT_//' \
        | sed 's/_PRODUCTION_.*//' \
        | sed 's/_SNAPSHOT_.*//' \
        | sed 's/_RELEASE_.*//')

    echo ""
    echo "=== Extension 적재: ${BASENAME} ==="
    echo "    이름: ${EXT_NAME}, effectiveTime: ${EXT_ET}"

    # 1. RF2 append 적재
    echo "    [1] RF2 데이터 적재 (append 모드)..."
    java -Xmx4g -cp "${CLASS_DIR}:${PG_JDBC_JAR}" \
        co.infoclinic.term.common.loader.SnomedRf2Loader "${ZIP}" --append
    echo "    ✓ RF2 적재 완료"

    # 2. REFERENCESET_ACTIVE 갱신
    echo "    [2] REFERENCESET_ACTIVE 갱신..."
    bash "${LOCATION}/refset_active_pg.sh"
    echo "    ✓ REFERENCESET_ACTIVE 완료"

    # 3. SCHEME 등록 (드롭박스 표시)
    if [ -n "${EXT_ET}" ]; then
        echo "    [3] SCHEME 등록..."
        ${PSQL} -c \
            "INSERT INTO scheme (id, name, edition, version, authority, date, extension_name)
             VALUES (
               'SNOMEDCT-${EXT_NAME}-v${EXT_ET}',
               'SNOMEDCT-${EXT_NAME}',
               '${EXT_NAME}',
               'v${EXT_ET}',
               'SNOMED International',
               '${EXT_ET}',
               '${EXT_NAME}'
             )
             ON CONFLICT (id) DO UPDATE SET date = EXCLUDED.date;"
        echo "    ✓ SCHEME 등록 완료 (${EXT_NAME} v${EXT_ET})"
    else
        echo "    ⚠ ZIP 파일명에서 effectiveTime 추출 실패 — SCHEME 등록 생략"
    fi

    echo "=== ${BASENAME} 완료 ==="
done

echo ""
echo "============================================================"
echo " 등록된 릴리즈 현황"
${PSQL} -c "SELECT id, version, extension_name FROM scheme ORDER BY date DESC, extension_name NULLS FIRST LIMIT 20;"
echo "============================================================"
echo "완료. 서버 재기동 후 드롭박스에 반영됩니다."
