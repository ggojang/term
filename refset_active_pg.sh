#!/bin/bash

#----------------------------------------------------------------
# REFERENCESET_ACTIVE 적재 스크립트 (PostgreSQL)
# REFERENCESET 테이블로부터 최신 활성 refset 멤버를 추출하여
# REFERENCESET_ACTIVE 테이블에 적재합니다.
#----------------------------------------------------------------

LOCATION="$(cd "$(dirname "$0")" && pwd)"

PG_HOST="127.0.0.1"
PG_PORT="5432"
PG_DB="term"
PG_USER="postgres"
PG_PASS="julab123!"

export PGPASSWORD="${PG_PASS}"

PSQL="psql -h ${PG_HOST} -p ${PG_PORT} -U ${PG_USER} -d ${PG_DB}"

echo "============================================"
echo " REFERENCESET_ACTIVE 적재 시작 (PostgreSQL)"
echo "============================================"

${PSQL} -v ON_ERROR_STOP=1 -f "${LOCATION}/refset_active_loader_pg.sql"

if [ $? -eq 0 ]; then
    echo "============================================"
    echo " REFERENCESET_ACTIVE 적재 완료!"
    echo "============================================"
else
    echo "적재 중 오류가 발생했습니다."
    exit 1
fi
