# ICD10 색인
#
# - 검색 요구사항
#   - 코드로 검색하고 일치하는 엔티티를 찾을 수 있어야 함.
#   - 코드가 아닌 용어 검색하고 preferred 또는 일부 일치하는 엔티티 목록을 찾을 수 있어야 함.
#
# 1. 코드
#   - ICD10 테이블: CODE 전체
# 2. 용어
#   - ICD10 테이블: CODE, KIND
# 스크립트 시작시간
CURRENT_DATE=$(date +"%Y%m%d_%H%M%S")

# (1) set up will be used variables
MYSQLHOST="127.0.0.1"
MYSQLPORT="3306"
MYSQLDB="icd10"
MYSQLUSER="root"

MYSQL_LOGIN_PATH="termclient-${CURRENT_DATE}"

#Elasticsearch
ES_MAPPING_TYPE="term_icd10_type"
ES_SETTING_FILE="@icd10_index_settings.json"
ES_MAPPING_FILE="@icd10_index_mapping.json"
LOGSTASH_CONF="icd10_index_io.conf"

ES_HOST="http://127.0.0.1:19210"

# ====================================================================
# Read More Information
# ====================================================================

echo "============================================"

echo "Elasticsearch Host:port[127.0.0.1:19210] " 

TMP_ES_HOST="127.0.0.1:19210"
ES_HOST=${TMP_ES_HOST:-$ES_HOST}

echo "Logstash Path (e.g. /home/devstudy/services/elastic/logstash-2.2.2/bin/logstash) " 
LOGSTASH="/home/devstudy/services/elastic/logstash-2.2.2/bin/logstash"

echo "============================================"

# 생성시 필요한 파일 명
OUT_FILE="/tmp/icd10_index.csv"

ES_IDX="icd10_2016"
LOGSTASH_TMP_CONF="logstash_icd10_idx.conf"

echo "============================================"

echo "이 스크립트는 데이터베이스의 파일권한을 획득한 사용자의 계정을 필요로 합니다."

echo "DB Host[$MYSQLHOST] "
TMP_HOST="127.0.0.1"
MYSQLHOST=${TMP_HOST:-$MYSQLHOST}

echo "DB Port[3306] " 
TMP_PORT="3306"
MYSQLPORT=${TMP_PORT:-$MYSQLPORT}

echo "DB User(e.g. root) "
MYSQLUSER="root"

echo "DB Schema[icd10] " 
TMP_MYSQLDB="icd10"
MYSQLDB=${TMP_MYSQLDB:-$MYSQLDB}

#MYSQLOPTS="--user=${MYSQLUSER} --password=${MYSQLPASS} --host=${MYSQLHOST} ${MYSQLDB}"
#MYSQLOPTS="--login-path=${MYSQL_LOGIN_PATH} ${MYSQLDB}"

echo "DB Password"
#mysql_config_editor set --login-path=${MYSQL_ICD10_PATH} --host=${MYSQLHOST} --user=${MYSQLUSER} --password --socket=/tmp/mysql.sock --port=${MYSQLPORT}

MYSQLPASS="julab123!"

echo

#mysql_config_editor print --all

echo "이전 ${OUT_FILE} 파일 삭제중..." 

sudo rm ${OUT_FILE}

echo "${MYSQLDB} Table에서 기준 색인 데이터를 파일로 내보내는 중..."

# 검색용 데이터 파일로 내보내기
#mysql ${MYSQLOPTS} << EOFMYSQL
mysql -uroot -p${MYSQLPASS} ${MYSQLDB} << EOFMYSQL

SELECT 	
	CODE, KIND, LABEL
INTO OUTFILE '${OUT_FILE}' 
FIELDS TERMINATED BY '\t' 
LINES TERMINATED BY '\r\n'
FROM icd10.ICD10_RUBRIC;

EOFMYSQL

#cat ${OUT_FILE} | more

# To import Elasticsearch using logstash
# The delete index API allows to delte an existing index
echo "${ES_HOST}에 ${ES_IDX} 인덱스가 존재하면 삭제"
curl --user ic_admin:infoclinic2! -XDELETE "${ES_HOST}/${ES_IDX}"

echo

# Create index with settings
echo "신규 인덱스 생성 및 셋팅"
curl --user ic_admin:infoclinic2! -XPUT ${ES_HOST}/${ES_IDX} --data-binary ${ES_SETTING_FILE}

echo
# Put mapping
echo "매핑파일 입력"
curl --user ic_admin:infoclinic2! -XPUT "${ES_HOST}/${ES_IDX}/_mapping/${ES_MAPPING_TYPE}" -d ${ES_MAPPING_FILE}

echo
#Logstash Config 옵션값 변경: Index, Host값 대체
#sed = Stream Editor
# s/original/new/
# s = the substitude command
sed -e "s@{ES_IDX}@${ES_IDX}@" -e "s@{ES_HOST}@${ES_HOST}@" < ${LOGSTASH_CONF} > ${LOGSTASH_TMP_CONF}
LOGSTASH_OPTS="-l elog.log -f ${LOGSTASH_TMP_CONF}"

#cat 인덱스파일.csv | logstash -l elog.log -f 옵션파일.conf
echo "Logstash 실행"
cat ${OUT_FILE} | ${LOGSTASH} ${LOGSTASH_OPTS} &

echo

echo "인덱스 alias 등록"
#원본파일(bak)에서 수정된 부분 : "'"${ES_IDX}"'" -> "${ES_IDX}"
curl --user ic_admin:infoclinic2! -XPOST "${ES_HOST}/_aliases" -d '{"actions":[{"add": {"index": "'"${ES_IDX}"'", "alias":"icd10_search"}}]}'

#echo "인덱스 csv 파일 삭제"
#sudo rm ${INDEX_FILE}
#sudo rm ${LOGSTASH_TMP_CONF}
