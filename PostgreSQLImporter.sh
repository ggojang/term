#!/bin/bash
#----------------------------------------------------------------
# SNOMED CT + LOINC + KCD-9 PostgreSQL 통합 임포터
#
# 적재 대상 (release_files/ 기준):
#   1. SnomedCT_InternationalRF2_*.zip  : 코어 테이블 (스키마 재생성)
#   2. SnomedCT_*_PRODUCTION_*.zip      : 익스텐션 ZIPs (append)
#   3. TransitiveClosure 생성
#   4. refset_active_pg.sh              : REFERENCESET_ACTIVE
#   5. mrcm_pg.sh                       : MRCM_CONSTRAINTS
#   6. LoincDataLoader                  : LOINC 테이블
#   7. KcdDataLoader                    : KCD-9 (ICD10_CLASS 한글 + KCD9_MORPH)
#
# 사용법:
#   ./PostgreSQLImporter.sh [release_files_dir]
#   (인자 생략 시 ~/services/term/release_files 사용)
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

RELEASE_DIR="${1:-${LOCATION}/release_files}"

if [ ! -d "${RELEASE_DIR}" ]; then
    echo "release_files 디렉토리가 없습니다: ${RELEASE_DIR}"
    exit 1
fi

# JDBC JAR 위치
PG_JDBC_JAR=$(find ~/.m2/repository/org/postgresql -name "postgresql-*.jar" 2>/dev/null | sort | tail -1)
if [ -z "${PG_JDBC_JAR}" ]; then
    PG_JDBC_JAR="${LOCATION}/postgresql.jar"
fi
if [ ! -f "${PG_JDBC_JAR}" ]; then
    echo "PostgreSQL JDBC JAR를 찾을 수 없습니다. mvn dependency:resolve 실행 또는 postgresql.jar 배치 필요"
    exit 1
fi

CLASS_DIR="/tmp/term_loader_classes"
mkdir -p "${CLASS_DIR}"

compile_loader() {
    local src="$1"
    echo "  컴파일: $(basename ${src})"
    javac -encoding UTF-8 -cp "${PG_JDBC_JAR}" -d "${CLASS_DIR}" "${src}"
}

echo "============================================================"
echo " SNOMED CT + LOINC + KCD-9 PostgreSQL 통합 임포터"
echo " release_files: ${RELEASE_DIR}"
echo " JDBC JAR     : ${PG_JDBC_JAR}"
echo " DB           : ${PG_HOST}:${PG_PORT}/${PG_DB}"
echo "============================================================"

# ── DB 확인/생성 ──────────────────────────────────────────────
DB_EXISTS=$(psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d postgres \
    -tAc "SELECT 1 FROM pg_database WHERE datname='${PG_DB}'" 2>/dev/null || echo "")
if [ -z "${DB_EXISTS}" ]; then
    echo "[준비] DB '${PG_DB}' 생성..."
    psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d postgres \
        -c "CREATE DATABASE ${PG_DB} ENCODING 'UTF8';"
fi

# ── 로더 소스 경로 ─────────────────────────────────────────────
LOADER_DIR="${LOCATION}/src/main/java/co/infoclinic/term/common/loader"

# ── STEP 1: International RF2 (스키마 재생성) ──────────────────
echo ""
echo "[1] SNOMED CT International RF2 적재 (스키마 재생성)..."
INT_ZIP=$(find "${RELEASE_DIR}" -maxdepth 1 -name "SnomedCT_InternationalRF2_*.zip" | sort | tail -1)
if [ -z "${INT_ZIP}" ]; then
    echo "    ERROR: SnomedCT_InternationalRF2_*.zip 파일이 없습니다."
    exit 1
fi
echo "    파일: ${INT_ZIP}"
compile_loader "${LOADER_DIR}/SnomedRf2Loader.java"
java -Xmx4g -cp "${CLASS_DIR}:${PG_JDBC_JAR}" \
    co.infoclinic.term.common.loader.SnomedRf2Loader "${INT_ZIP}"
echo "    ✓ International 적재 완료"

# ── STEP 2: Extension ZIPs (append 모드) ──────────────────────
echo ""
echo "[2] SNOMED CT Extension ZIPs 적재 (append 모드)..."
EXT_ZIPS=$(find "${RELEASE_DIR}" -maxdepth 1 -name "SnomedCT_*.zip" \
    ! -name "SnomedCT_InternationalRF2_*.zip" | sort)
if [ -z "${EXT_ZIPS}" ]; then
    echo "    (익스텐션 ZIP 없음 — 건너뜀)"
else
    for ZIP in ${EXT_ZIPS}; do
        echo "    파일: $(basename ${ZIP})"
        java -Xmx4g -cp "${CLASS_DIR}:${PG_JDBC_JAR}" \
            co.infoclinic.term.common.loader.SnomedRf2Loader "${ZIP}" --append
        echo "    ✓ 완료"
    done
fi

# ── STEP 3: Transitive Closure ────────────────────────────────
echo ""
echo "[3] Transitive Closure 생성..."
compile_loader "${LOADER_DIR}/TransitiveClosureLoader.java"

# International ZIP 파일명에서 effectiveTime 추출 (예: SnomedCT_InternationalRF2_PRODUCTION_20241001T120000Z.zip → 20241001)
INT_ET=$(basename "${INT_ZIP}" | grep -oE '[0-9]{8}' | head -1)
if [ -z "${INT_ET}" ]; then
    echo "    ⚠ ZIP 파일명에서 effectiveTime 추출 실패. inferred_relationship 최신값 사용."
    INT_ET=""
fi

echo "    International effectiveTime: ${INT_ET:-<auto>}"
java -Xmx6g -cp "${CLASS_DIR}:${PG_JDBC_JAR}" \
    co.infoclinic.term.common.loader.TransitiveClosureLoader "${INT_ET}"
echo "    ✓ TC 생성 완료 (effectiveTime=${INT_ET:-auto}) — TC_META는 Loader가 자동 갱신"

# International 릴리즈를 SCHEME 테이블에 등록/갱신
if [ -n "${INT_ET}" ]; then
    INT_NAME=$(basename "${INT_ZIP}" | sed 's/\.zip$//')
    echo "    SCHEME 등록: SNOMEDCT v${INT_ET} (International)"
    psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB} -c \
        "INSERT INTO scheme (id, name, edition, version, authority, date, extension_name)
         VALUES ('SNOMEDCT-INT-v${INT_ET}', 'SNOMEDCT-INT', 'INT', 'v${INT_ET}', 'SNOMED International', '${INT_ET}', NULL)
         ON CONFLICT (id) DO UPDATE SET date = EXCLUDED.date;"
fi

# Extension ZIP들의 effectiveTime 추출 및 SCHEME 등록
if [ -n "${EXT_ZIPS}" ]; then
    for ZIP in ${EXT_ZIPS}; do
        EXT_ET=$(basename "${ZIP}" | grep -oE '[0-9]{8}' | head -1)
        EXT_NAME=$(basename "${ZIP}" | sed 's/SnomedCT_//' | sed 's/_PRODUCTION_.*//' | sed 's/_SNAPSHOT_.*//')
        if [ -n "${EXT_ET}" ]; then
            echo "    SCHEME 등록: ${EXT_NAME} v${EXT_ET} (Extension)"
            psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB} -c \
                "INSERT INTO scheme (id, name, edition, version, authority, date, extension_name)
                 VALUES ('SNOMEDCT-${EXT_NAME}-v${EXT_ET}', 'SNOMEDCT-${EXT_NAME}', '${EXT_NAME}', 'v${EXT_ET}', 'SNOMED International', '${EXT_ET}', '${EXT_NAME}')
                 ON CONFLICT (id) DO UPDATE SET date = EXCLUDED.date;"
        fi
    done
fi

# ── STEP 4: REFERENCESET_ACTIVE ───────────────────────────────
echo ""
echo "[4] REFERENCESET_ACTIVE 적재..."
bash "${LOCATION}/refset_active_pg.sh"
echo "    ✓ REFERENCESET_ACTIVE 완료"

# ── STEP 5: MRCM ──────────────────────────────────────────────
echo ""
echo "[5] MRCM_CONSTRAINTS 적재..."
if [ -f "${RELEASE_DIR}/MRCM_CONSTRAINTS.txt" ]; then
    bash "${LOCATION}/mrcm_pg.sh" "${RELEASE_DIR}/MRCM_CONSTRAINTS.txt" || \
        echo "    ⚠ MRCM 오류 (계속 진행)"
else
    bash "${LOCATION}/mrcm_pg.sh" || \
        echo "    ⚠ MRCM 파일 없음 (건너뜀)"
fi
echo "    ✓ MRCM 완료"

# ── STEP 6: LOINC ─────────────────────────────────────────────
echo ""
echo "[6] LOINC 적재..."
LOINC_CLASS_CSV="${RELEASE_DIR}/loinc_2.82_class.csv"
LOINC_BASE_DIR=$(find "${RELEASE_DIR}/loinc" -maxdepth 2 -name "LoincTable" 2>/dev/null | head -1 | xargs dirname 2>/dev/null || echo "")

if [ ! -f "${LOINC_CLASS_CSV}" ]; then
    echo "    ⚠ loinc_2.82_class.csv 없음 (건너뜀)"
else
    compile_loader "${LOADER_DIR}/LoincDataLoader.java"
    if [ -n "${LOINC_BASE_DIR}" ]; then
        echo "    CLASS: ${LOINC_CLASS_CSV}"
        echo "    BASE : ${LOINC_BASE_DIR}"
        java -Xmx2g -cp "${CLASS_DIR}:${PG_JDBC_JAR}" \
            co.infoclinic.term.common.loader.LoincDataLoader \
            "${LOINC_CLASS_CSV}" "${LOINC_BASE_DIR}"
    else
        echo "    BASE 없음 — CLASS만 적재"
        java -Xmx2g -cp "${CLASS_DIR}:${PG_JDBC_JAR}" \
            co.infoclinic.term.common.loader.LoincDataLoader \
            "${LOINC_CLASS_CSV}"
    fi
    echo "    ✓ LOINC 완료"
fi

# ── STEP 7: KCD-9 ─────────────────────────────────────────────
echo ""
echo "[7] KCD-9 적재..."
KCD9_DIR="${RELEASE_DIR}/KCD-9"
if [ ! -d "${KCD9_DIR}" ] || [ ! -f "${KCD9_DIR}/kcd9_main.tsv" ]; then
    echo "    ⚠ KCD-9 파일 없음 (건너뜀)"
else
    compile_loader "${LOADER_DIR}/KcdDataLoader.java"
    java -Xmx1g -cp "${CLASS_DIR}:${PG_JDBC_JAR}" \
        co.infoclinic.term.common.loader.KcdDataLoader "${KCD9_DIR}"
    echo "    ✓ KCD-9 완료"
fi

# ── STEP 8: SEARCH_INDEX ──────────────────────────────────────
echo ""
echo "[8] SEARCH_INDEX 생성 (Mapping Support 검색 인덱스)..."
compile_loader "${LOADER_DIR}/SearchIndexLoader.java"
java -Xmx4g -cp "${CLASS_DIR}:${PG_JDBC_JAR}"     co.infoclinic.term.common.loader.SearchIndexLoader
echo "    ✓ SEARCH_INDEX 완료"

# ── STEP 9: UMLS SYNONYM ──────────────────────────────────────
echo ""
echo "[9] UMLS 동의어 적재..."
compile_loader "${LOADER_DIR}/UmlsSynonymLoader.java"
java -Xmx1g -cp "${CLASS_DIR}:${PG_JDBC_JAR}"     co.infoclinic.term.common.loader.UmlsSynonymLoader     "${LOCATION}/map_index/UMLS_SYNONYM"
echo "    ✓ UMLS 동의어 완료"

# ── 정리 ──────────────────────────────────────────────────────
rm -rf "${CLASS_DIR}"

echo ""
echo "============================================================"
echo " 전체 임포트 완료!"
echo " http://115.68.110.23/stom/ 에서 확인하세요."
echo "============================================================"
