#!/bin/bash

#----------------------------------------------------------------
# MRCM_CONSTRAINTS 적재 스크립트 (PostgreSQL)
#----------------------------------------------------------------

LOCATION="$(cd "$(dirname "$0")" && pwd)"

PG_HOST="127.0.0.1"
PG_PORT="5432"
PG_DB="term"
PG_USER="postgres"
PG_PASS="julab123!"

# MRCM TSV 파일 경로 (기본값: 스크립트와 같은 디렉토리)
MRCM_FILE="${1:-${LOCATION}/MySQL/MRCM/MRCM_CONSTRAINTS_20220228.txt}"

if [ ! -f "${MRCM_FILE}" ]; then
    echo "MRCM 파일이 없습니다: ${MRCM_FILE}"
    echo "MRCM_FILE 변수를 수정하거나 파일을 해당 경로에 위치시킨 후 다시 실행하세요."
    exit 1
fi

export PGPASSWORD="${PG_PASS}"
PSQL="psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB}"

echo "============================================"
echo " MRCM_CONSTRAINTS 적재 시작 (PostgreSQL)"
echo " 파일: ${MRCM_FILE}"
echo "============================================"

${PSQL} -v ON_ERROR_STOP=1 -v mrcm_file="${MRCM_FILE}" \
        -f "${LOCATION}/mrcm_loader_pg.sql"

if [ $? -eq 0 ]; then
    echo "MRCM_CONSTRAINTS 적재 완료!"
else
    echo "적재 중 오류가 발생했습니다."
    exit 1
fi
