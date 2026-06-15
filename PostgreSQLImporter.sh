#!/bin/bash

#----------------------------------------------------------------
# SNOMED CT PostgreSQL 통합 임포터
#
# 실행 순서:
#   1. SnomedRf2Loader.java  : 코어 테이블 + REFERENCESET 적재
#   2. TransitiveClosureGeneratorFromInferred.sh : TC 테이블 생성
#   3. refset_active_pg.sh   : REFERENCESET_ACTIVE 적재
#   4. mrcm_pg.sh            : MRCM_CONSTRAINTS 적재
#
# 사용법:
#   ./PostgreSQLImporter.sh /path/to/SnomedCT_InternationalRF2_PRODUCTION_20260601T120000Z.zip
#
# 전제 조건:
#   - PostgreSQL 실행 중, DB 'term' 존재, psql PATH 등록
#   - Java (17+) 및 Maven 설치, postgresql JDBC JAR 존재
#   - PGPASSWORD 또는 .pgpass 설정 (기본값 julab123!)
#----------------------------------------------------------------

set -e

LOCATION="$(cd "$(dirname "$0")" && pwd)"

# ── 설정 ──────────────────────────────────────────────────────
PG_HOST="127.0.0.1"
PG_PORT="5432"
PG_DB="term"
PG_USER="postgres"
PG_PASS="julab123!"

# JDBC JAR 위치 (mvn 로컬 리포지토리 또는 직접 지정)
PG_JDBC_JAR=$(find ~/.m2/repository/org/postgresql -name "postgresql-*.jar" 2>/dev/null | sort | tail -1)
if [ -z "${PG_JDBC_JAR}" ]; then
    PG_JDBC_JAR="${LOCATION}/postgresql.jar"
fi

# ── 인자 확인 ─────────────────────────────────────────────────
if [ $# -lt 1 ]; then
    echo "사용법: $0 <RF2-zip-파일경로>"
    echo "예)    $0 /data/SnomedCT_InternationalRF2_PRODUCTION_20260601T120000Z.zip"
    exit 1
fi
RF2_ZIP="$1"

if [ ! -f "${RF2_ZIP}" ]; then
    echo "파일이 존재하지 않습니다: ${RF2_ZIP}"
    exit 1
fi

if [ ! -f "${PG_JDBC_JAR}" ]; then
    echo "PostgreSQL JDBC JAR를 찾을 수 없습니다: ${PG_JDBC_JAR}"
    echo "maven 의존성을 설치하거나 postgresql.jar를 스크립트 디렉토리에 두세요."
    exit 1
fi

export PGPASSWORD="${PG_PASS}"

echo "============================================================"
echo " SNOMED CT PostgreSQL 통합 임포터"
echo " RF2 ZIP  : ${RF2_ZIP}"
echo " JDBC JAR : ${PG_JDBC_JAR}"
echo " DB       : ${PG_HOST}:${PG_PORT}/${PG_DB}"
echo "============================================================"

# ── DB 생성 (없는 경우) ───────────────────────────────────────
echo ""
echo "[0/4] DB '${PG_DB}' 존재 확인..."
DB_EXISTS=$(psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d postgres \
    -tAc "SELECT 1 FROM pg_database WHERE datname='${PG_DB}'" 2>/dev/null || echo "")
if [ -z "${DB_EXISTS}" ]; then
    echo "      DB '${PG_DB}'가 없습니다. 생성합니다..."
    psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d postgres \
        -c "CREATE DATABASE ${PG_DB} ENCODING 'UTF8';"
fi

# ── STEP 1: SnomedRf2Loader ───────────────────────────────────
echo ""
echo "[1/4] SnomedRf2Loader 컴파일 및 실행 중..."
echo "      (스키마 DROP → 재생성 → 코어 테이블 + REFERENCESET 적재)"

RF2_LOADER_SRC="${LOCATION}/src/main/java/co/infoclinic/term/common/loader/SnomedRf2Loader.java"
RF2_LOADER_CLASS_DIR="/tmp/snomed_rf2_loader_classes"
mkdir -p "${RF2_LOADER_CLASS_DIR}"

javac -encoding UTF-8 -cp "${PG_JDBC_JAR}" \
    -d "${RF2_LOADER_CLASS_DIR}" "${RF2_LOADER_SRC}"

java -Xmx4g -cp "${RF2_LOADER_CLASS_DIR}:${PG_JDBC_JAR}" \
    co.infoclinic.term.common.loader.SnomedRf2Loader "${RF2_ZIP}"

echo "      ✓ 코어 테이블 적재 완료"

# ── STEP 2: TC 생성 ───────────────────────────────────────────
echo ""
echo "[2/4] Transitive Closure 생성 중..."
cd "${LOCATION}"
bash TransitiveClosureGeneratorFromInferred.sh
echo "      ✓ TC 생성 완료"

# ── STEP 3: REFERENCESET_ACTIVE ───────────────────────────────
echo ""
echo "[3/4] REFERENCESET_ACTIVE 적재 중..."
bash "${LOCATION}/refset_active_pg.sh"
echo "      ✓ REFERENCESET_ACTIVE 완료"

# ── STEP 4: MRCM ──────────────────────────────────────────────
echo ""
echo "[4/4] MRCM_CONSTRAINTS 적재 중..."
if bash "${LOCATION}/mrcm_pg.sh"; then
    echo "      ✓ MRCM_CONSTRAINTS 완료"
else
    echo "      ⚠ MRCM 파일이 없거나 오류 발생 (선택 항목이므로 계속 진행)"
fi

# ── 정리 ──────────────────────────────────────────────────────
rm -rf "${RF2_LOADER_CLASS_DIR}"

echo ""
echo "============================================================"
echo " 임포트 완료! http://localhost:8080/stom/ 에서 확인하세요."
echo "============================================================"
