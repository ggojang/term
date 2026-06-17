-- HIRA 보험인정기준 다운로드 이력 테이블
-- 실행: psql -U postgres -d term -f create_table.sql

SET search_path = term;

-- ── 다운로드 이력 (단일 테이블) ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS hira_downloads (
    id            SERIAL PRIMARY KEY,
    category      VARCHAR(20)   NOT NULL,          -- 행위 / 약제 / 치료재료
    file_type     VARCHAR(10)   NOT NULL,          -- 마스터 / 고시
    post_no       VARCHAR(50)   NOT NULL UNIQUE,   -- 게시글 고유번호 (중복 방지)
    title         VARCHAR(500)  NOT NULL,
    post_date     DATE          NOT NULL,
    filename      VARCHAR(500),
    saved_path    VARCHAR(1000),
    file_hash     VARCHAR(64),                     -- SHA-256
    applied       BOOLEAN       DEFAULT FALSE,     -- 코드 테이블 적재 완료 여부
    applied_at    TIMESTAMPTZ,
    downloaded_at TIMESTAMPTZ   DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_hira_downloads_cat_type_date
    ON hira_downloads (category, file_type, post_date DESC);
CREATE INDEX IF NOT EXISTS idx_hira_downloads_applied
    ON hira_downloads (applied, post_date);

-- ── 코드 적재 테이블 (파일 파싱 후 별도 작업) ──────────────────────────────────
-- 파일 파싱·적재 시점에 아래 테이블 생성 예정
--
-- CREATE TABLE IF NOT EXISTS hira_행위_code ( ... );
-- CREATE TABLE IF NOT EXISTS hira_약제_code ( ... );
-- CREATE TABLE IF NOT EXISTS hira_치료재료_code ( ... );
--
-- 컬럼 구성은 마스터파일 실제 구조 확인 후 결정

COMMENT ON TABLE hira_downloads IS 'HIRA 보험인정기준 파일 다운로드 이력';
COMMENT ON COLUMN hira_downloads.file_type IS '마스터 또는 고시';
COMMENT ON COLUMN hira_downloads.applied   IS '코드 테이블(행위/약제/치료재료) 적재 완료 여부';
