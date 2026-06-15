#!/bin/bash


LOCATION="/home/devstudy/resources/importer/loinc"

echo "MySQL 유저명을 입력해주세요."
read MYSQL_USER

echo "MySQL 비밀번호를 입력해주세요."
read MYSQL_PASS

echo "LOINC 스키마명을 입력해주세요."
read LOINC_SCHEMA

echo "새로 생성할 유저명을 입력해주세요."
read NEW_USER

echo "새 유저의 비밀번호를 입력해주세요."
read NEW_PASS

mysql -u${MYSQL_USER} -p${MYSQL_PASS} ${LOINC_SCHEMA} < ${LOCATION}/MySQL/tDDL.sql

mysql -u${MYSQL_USER} -p${MYSQL_PASS} --local-infile ${LOINC_SCHEMA} < ${LOCATION}/MySQL/tLoader.sql

