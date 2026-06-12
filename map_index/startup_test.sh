#!/bin/bash

#----------------------------------------------------------------
# a simple index generate and import script for SNOMEDCT Internaitonal Release
# version 1, updated October 18, 2016.
# copyright 2016 infoclinic co.,ltd, http://infoclinic.co
#----------------------------------------------------------------

# ref: https://www.tectut.com/2014/05/export-mysql-query-results-to-csv-using-shell-script/
# ref: http://mapoo.net/db/dbmysql/mysql-56-mysql_config_editor/

# 스크립트 시작시간
CURRENT_DATE=$(date +"%Y%m%d_%H%M%S")

# (1) set up will be used variables
MYSQLHOST="127.0.0.1"
MYSQLPORT="3306"
MYSQLDB="term"
MYSQLUSER="root"
MYSQLPASS="julab123!"

MYSQL_LOGIN_PATH="termclient-${CURRENT_DATE}"

EFFECTIVE_TIME="20260501"
ISA_REL="116680003"

FSN="900000000000003001"
SYNONYM="900000000000013009"
DEFINITION="900000000000550004"
PRIMITIVE="900000000000074008"

#테이블 명
CONCEPT_TABLE="CONCEPT"
DESCRIPTION_TABLE="DESCRIPTION"
MRCM_TABLE="MRCM_CONSTRAINTS"
REL_TABLE="INFERRED_RELATIONSHIP"
REFERENCESET_TABLE="REFERENCESET"

#Elasticsearch
ES_MAPPING_TYPE="term_snomedct_type"
ES_SETTING_FILE="@search_index_settings_test.json"
ES_MAPPING_FILE="@search_index_mapping_test.json"
LOGSTASH_CONF="search_index_test.conf"

ES_HOST="http://127.0.0.1:19210"

# ====================================================================
# Read More Information
# ====================================================================

echo "============================================"

#read -p "SNOMED CT EffectiveTime => " EFFECTIVE_TIME
echo "EFFECTIVE TIME : " ${EFFECTIVE_TIME}

#read -p "Elasticsearch Host:port[127.0.0.1:19210] => " TMP_ES_HOST
TMP_ES_HOST="127.0.0.1:19210"
ES_HOST=${TMP_ES_HOST:-$ES_HOST}

#read -p "Logstash Path (e.g. /usr/share/logstash/bin/logstash) => " LOGSTASH
LOGSTASH="/home/devstudy/services/elastic/logstash-2.2.2/bin/logstash"

echo "============================================"

# 생성시 필요한 파일 명
OUT_FILE="/tmp/SCT_IDX_OUT_${EFFECTIVE_TIME}_test.csv"
INDEX_FILE="/tmp/SCT_IDX_${EFFECTIVE_TIME}_test.csv"
MRCM_FILE="/tmp/SCT_IDX_MRCM_${EFFECTIVE_TIME}_test.csv"
ISA_FILE="/tmp/SCT_IDX_ISA_${EFFECTIVE_TIME}_test.csv"

ES_IDX="snomedct_${EFFECTIVE_TIME}_test"
LOGSTASH_TMP_CONF="logstash_sctidx_${EFFECTIVE_TIME}_test.conf"

echo "============================================"

echo "이 스크립트는 데이터베이스의 파일권한을 획득한 사용자의 계정을 필요로 합니다."

#read -p "DB Host[$MYSQLHOST] => " TMP_HOST
TMP_HOST="127.0.0.1"
MYSQLHOST=${TMP_HOST:-$MYSQLHOST}

#read -p "DB Port[3306] => " TMP_PORT
TMP_PORT="3306"
MYSQLPORT=${TMP_PORT:-$MYSQLPORT}

#read -p "DB User(e.g. root) => " MYSQLUSER

#read -p "DB Schema[term] => " TMP_MYSQLDB
TMP_MYSQLDB="term"
MYSQLDB=${TMP_MYSQLDB:-$MYSQLDB}

#MYSQLOPTS="--user=${MYSQLUSER} --password=${MYSQLPASS} --host=${MYSQLHOST} ${MYSQLDB}"
#MYSQLOPTS="--login-path=${MYSQL_LOGIN_PATH} ${MYSQLDB}"

#echo "DB Password"
#mysql_config_editor set --login-path=${MYSQL_LOGIN_PATH} --host=${MYSQLHOST} --user=${MYSQLUSER} --password --socket=/tmp/mysql.sock --port=${MYSQLPORT}

echo

#mysql_config_editor print --all


echo "${MYSQLDB}.${DESCRIPTION_TABLE},${CONCEPT_TABLE}에서 ${EFFECTIVE_TIME}기준 색인 데이터를 파일로 내보내는 중..."

#mysql ${MYSQLOPTS} << EOFMYSQL
mysql -uroot -p${MYSQLPASS} ${MYSQLDB} << EOFMYSQL

# 검색용 데이터 파일로 내보내기
SELECT
    d.CONCEPT_ID AS CID,
    d.DESCRIPTION_ID AS DID,
    GREATEST(c.EFFECTIVE_TIME, d.EFFECTIVE_TIME) AS CPETIME, -- as componentEffectiveTime
    c.EFFECTIVE_TIME AS CETIME, -- conceptEffectiveTime,
    d.EFFECTIVE_TIME AS DETIME, -- as descriptionEffectiveTime,
    CONCAT(JOIN_MAX_CONCEPT.EFFECTIVE_TIME,"+",JOIN_MAX_DESCRIPTION.EFFECTIVE_TIME,"+",
      CASE
			   WHEN c.ACTIVE =1 && d.ACTIVE =1
         THEN 1
         ELSE 0
	    END
    ) AS MCDEA, -- as maxConceptDescriptionEffectiveTimeAndActive
    CASE
	    WHEN c.ACTIVE = 1 && d.ACTIVE = 1
	    THEN 1
	    ELSE 0
    END AS CPA, -- as componentActive,
    CAST(c.ACTIVE as CHAR(1)) AS CA, -- as conceptActive
    CAST(d.ACTIVE as CHAR(1)) AS DA, -- as descriptionActive
    REPLACE(d.TERM, "\"", "") AS TERM, -- as term,
    REPLACE(substring_index(JOIN_FSN.TERM, "(", -1), ")", "") AS ST, -- as semanticTag
    REPLACE(JOIN_FSN.TERM, "\"", "") AS FSN, -- as fsn
    CASE
	   WHEN c.DEFINITION_STATUS_ID = ${PRIMITIVE}
        THEN 1
        ELSE 0
    END AS PRIMITIVE,  --  as definitionStatus  primitive? fully defined
    CASE
		WHEN d.TYPE_ID = ${FSN} -- fsn
        THEN 1
        WHEN d.TYPE_ID = ${SYNONYM} -- synonym
        THEN 3
        WHEN d.TYPE_ID = ${DEFINITION} -- definition
        THEN 4
        ELSE 0
    END AS TYPE, -- as type fsn? synonym, definition
    r.FIELD1 AS ACCEPT,
    length(d.TERM) AS LEN, -- as length,
    d.LANGUAGE_CODE AS LANG --  as lang
INTO OUTFILE '${INDEX_FILE}' FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n'
FROM ${MYSQLDB}.${CONCEPT_TABLE} as c
INNER JOIN ${MYSQLDB}.${DESCRIPTION_TABLE} as d
ON d.CONCEPT_ID = c.CONCEPT_ID
  AND d.LANGUAGE_CODE = 'en'
INNER JOIN ${MySQLDB}.${REFERENCESET_TABLE} as r
ON r.REFSET_ID = '900000000000509007'    -- US Language Reference Set
  AND d.DESCRIPTION_ID = r.REFERENCED_COMPONENT_ID
INNER JOIN (
  SELECT FSND.TERM, FSND.CONCEPT_ID, FSND.EFFECTIVE_TIME
  FROM ${MYSQLDB}.${DESCRIPTION_TABLE} as FSND
  WHERE FSND.TYPE_ID=${FSN} -- fsn
  AND FSND.ACTIVE=1
  AND FSND.LANGUAGE_CODE = 'en'
) as JOIN_FSN
ON JOIN_FSN.CONCEPT_ID = c.CONCEPT_ID
AND JOIN_FSN.EFFECTIVE_TIME = (
  SELECT MAX(SUB_FSND.EFFECTIVE_TIME)
  FROM ${MYSQLDB}.${DESCRIPTION_TABLE} as SUB_FSND
  WHERE SUB_FSND.TYPE_ID = ${FSN} -- fsn
    AND SUB_FSND.ACTIVE=1
    AND SUB_FSND.LANGUAGE_CODE = 'en'
    AND SUB_FSND.CONCEPT_ID=c.CONCEPT_ID
)
INNER JOIN (
  SELECT JMD.CONCEPT_ID, JMD.DESCRIPTION_ID, JMD.EFFECTIVE_TIME
  FROM ${MYSQLDB}.${DESCRIPTION_TABLE} as JMD
  WHERE JMD.LANGUAGE_CODE = 'en'
) as JOIN_MAX_DESCRIPTION
ON JOIN_MAX_DESCRIPTION.CONCEPT_ID = c.CONCEPT_ID
  AND JOIN_MAX_DESCRIPTION.DESCRIPTION_ID = d.DESCRIPTION_ID
  AND JOIN_MAX_DESCRIPTION.EFFECTIVE_TIME = (
  SELECT MAX(SUB_JMD.EFFECTIVE_TIME)
  FROM ${MYSQLDB}.${DESCRIPTION_TABLE} as SUB_JMD
  WHERE SUB_JMD.CONCEPT_ID = c.CONCEPT_ID
    AND SUB_JMD.DESCRIPTION_ID = d.DESCRIPTION_ID
    AND SUB_JMD.LANGUAGE_CODE = 'en'
)
INNER JOIN (
  SELECT MAX(JMC.EFFECTIVE_TIME) as EFFECTIVE_TIME, JMC.CONCEPT_ID
  FROM ${MYSQLDB}.${CONCEPT_TABLE} AS JMC
  GROUP BY JMC.CONCEPT_ID
)as JOIN_MAX_CONCEPT
ON JOIN_MAX_CONCEPT.CONCEPT_ID = c.CONCEPT_ID;

EOFMYSQL

# MRCM Export
#mysql ${MYSQLOPTS} << EOFMYSQL
mysql -uroot -p${MYSQLPASS} ${MYSQLDB} << EOFMYSQL


# MRCM 데이터 파일로 내보내기
SELECT ATTRIBUTE_ID, ATTRIBUTE_NAME, VALUE_ID, VALUE_NAME
INTO OUTFILE '${MRCM_FILE}' FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n' 
FROM ${MYSQLDB}.${MRCM_TABLE}
GROUP BY VALUE_ID, ATTRIBUTE_ID;

EOFMYSQL

# EffectiveTime기준 Active인 IS-A 관계 데이터를(여기서는 PRETC) CSV파일로 내보내기
# 파일이 저장되는 위치는 /tmp로 제한된다. 단, SELECT @@tmpdir;의 경로를 변경하여야 한다.  
echo "${MYSQLDB}.${REL_TABLE}에서 ${EFFECTIVE_TIME}기준 ISA 데이터를 파일로 내보내는 중..."
#mysql ${MYSQLOPTS} << EOFMYSQL
mysql -uroot -p${MYSQLPASS} ${MYSQLDB} << EOFMYSQL
SELECT r.SOURCE_ID as CHILD_ID, r.DESTINATION_ID as PARENT_ID
INTO OUTFILE '${ISA_FILE}' FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n'
FROM ${MYSQLDB}.${REL_TABLE} r
  INNER JOIN (
    SELECT RELATIONSHIP_ID, SOURCE_ID, MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME
    FROM ${REL_TABLE}
    WHERE EFFECTIVE_TIME <= ${EFFECTIVE_TIME}
    AND TYPE_ID = ${ISA_REL}
    GROUP BY RELATIONSHIP_ID
  ) as mr
  ON r.RELATIONSHIP_ID = mr.RELATIONSHIP_ID
  AND r.SOURCE_ID = mr.SOURCE_ID
  AND r.EFFECTIVE_TIME = mr.MAX_EFFECTIVE_TIME
  AND r.ACTIVE = 1;
EOFMYSQL


# MYSQL 로그인정보 삭제
#mysql_config_editor remove --login-path=${MYSQL_LOGIN_PATH}


# MrcmAppender compile
javac -d . MrcmAppender.java

# MrcmAppender execute
echo "Index파일과 Mrcm 파일 결합중"
java -cp . co.infoclinic.term.snomedct.MrcmAppender ${ISA_FILE} ${INDEX_FILE} ${MRCM_FILE} ${OUT_FILE}
echo "Index파일과 Mrcm 파일 결합완료"


# To import Elasticsearch using logstash
# The delete index API allows to delte an existing index
echo "${ES_HOST}에 ${ES_IDX} 인덱스가 존재하면 삭제"
curl --user ic_admin:infoclinic2! -XDELETE "${ES_HOST}/${ES_IDX}"

echo

# Create index with settings
echo "신규 인덱스 생성 및 셋팅"
#curl --user ic_admin:infoclinic2! ${ES_HOST}/${ES_IDX} --data-binary ${ES_SETTING_FILE}
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
cat ${OUT_FILE} | ${LOGSTASH} ${LOGSTASH_OPTS}

#echo

#echo "인덱스 alias 등록"
#원본파일(bak)에서 수정된 부분 : "'"${ES_IDX}"'" -> "${ES_IDX}"
#curl --user elastic:changeme -XPOST '"'${ES_HOST}'"/_aliases' -d '{"actions":[{"add": {"index": "'"${ES_IDX}"'", "alias":"snomedct_test"}}]}'
curl --user ic_admin:infoclinic2! -XPOST "${ES_HOST}/_aliases" -d '{"actions":[{"add": {"index": "'"${ES_IDX}"'", "alias":"snomedct_test"}}]}'
#echo "인덱스 csv 파일 삭제"
#sudo rm ${INDEX_FILE}
#sudo rm ${LOGSTASH_TMP_CONF}

#echo "curl -u ic_admin -XGET 'http://localhost:19210/snomedct_test' then type infoclinic2!

