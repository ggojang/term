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

mysql -u${MYSQL_USER} -p${MYSQL_PASS} -e "DROP DATABASE IF EXISTS ${LOINC_SCHEMA} "
mysql -u${MYSQL_USER} -p${MYSQL_PASS} -e "CREATE DATABASE IF NOT EXISTS ${LOINC_SCHEMA} DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
#mysql -u${MYSQL_USER} -p${MYSQL_PASS} -e "CREATE USER '${NEW_USER}'@'%' IDENTIFIED BY '${NEW_PASS}';"
#mysql -u${MYSQL_USER} -p${MYSQL_PASS} -e "GRANT ALL PRIVILEGES ON ${LOINC_SCHEMA}.* TO '${NEW_USER}'@'%';"
#mysql -u${MYSQL_USER} -p${MYSQL_PASS} -e "FLUSH PRIVILEGES;"

mysql -u${MYSQL_USER} -p${MYSQL_PASS} ${LOINC_SCHEMA} < ${LOCATION}/MySQL/MySQLDDL.sql

mysql -u${MYSQL_USER} -p${MYSQL_PASS} --local-infile ${LOINC_SCHEMA} < ${LOCATION}/MySQL/MySQLLoader.sql

mysql -u${MYSQL_USER} -p${MYSQL_PASS} ${LOINC_SCHEMA} < ${LOCATION}/MySQL/MySQL_additional.sql | tee "result.txt"

mysql -u${MYSQL_USER} -p${MYSQL_PASS} ${LOINC_SCHEMA} < ${LOCATION}/MySQL/MySQL_additional2.sql | tee -a "result.txt"

mysql -u${MYSQL_USER} -p${MYSQL_PASS} ${LOINC_SCHEMA} < ${LOCATION}/MySQL/MySQL_additional3.sql | tee -a "result.txt"
