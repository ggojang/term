-- PostgreSQL 버전: MRCM_CONSTRAINTS 적재 스크립트
-- 탭 구분 TSV 파일을 COPY 명령으로 적재합니다.
-- 실행 전 :mrcm_file 변수를 지정하세요.
--   psql ... -v mrcm_file=/path/to/MRCM_CONSTRAINTS_20220228.txt -f mrcm_loader_pg.sql

SET search_path TO term, public;

TRUNCATE TABLE mrcm_constraints;

\COPY mrcm_constraints (ATTRIBUTE_ID, ATTRIBUTE_NAME, SOURCE_ID, SOURCE_NAME, VALUE_ID, VALUE_NAME)
FROM :'mrcm_file'
WITH (FORMAT TEXT, DELIMITER E'\t', HEADER FALSE);
