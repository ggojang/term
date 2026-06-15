#!/bin/bash


LOCATION="/home/devstudy/resources/importer/icd10"


echo "MySQL 유저명을 입력해주세요."
read MYSQL_USER

echo "MySQL 비밀번호를 입력해주세요."
read MYSQL_PASS

echo "ICD10 스키마명을 입력해주세요."
read ICD10_SCHEMA

mysql -u${MYSQL_USER} -p${MYSQL_PASS} ${ICD10_SCHEMA} < ${LOCATION}/ICD10_MySQLDDL.sql

mysql -u${MYSQL_USER} -p${MYSQL_PASS} --local-infile ${ICD10_SCHEMA} < ${LOCATION}/ICD10_MySQLLoader.sql

mysql -u${MYSQL_USER} -p${MYSQL_PASS} ${ICD10_SCHEMA} < ${LOCATION}/ICD10_Additional.sql
