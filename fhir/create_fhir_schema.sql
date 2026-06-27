-- FHIR Terminology Server DB 스키마
-- 대상 DB: term (PostgreSQL)
-- 실행: psql -U postgres -d term -f create_fhir_schema.sql

CREATE SCHEMA IF NOT EXISTS fhir;

-- FHIR 리소스 저장 테이블
-- CodeSystem, ValueSet, ConceptMap, NamingSystem 등 모든 FHIR 리소스를 JSON으로 저장
CREATE TABLE IF NOT EXISTS fhir.resource (
    resource_type VARCHAR(50)   NOT NULL,          -- CodeSystem | ValueSet | ConceptMap | NamingSystem | ...
    id            VARCHAR(255)  NOT NULL,           -- 리소스 ID (URL 마지막 세그먼트 또는 UUID)
    url           VARCHAR(500),                     -- canonical URL
    version       VARCHAR(50),                      -- 리소스 버전
    name          VARCHAR(255),                     -- machine-readable name
    title         VARCHAR(500),                     -- human-readable title
    status        VARCHAR(20),                      -- active | draft | retired | unknown
    content       TEXT         NOT NULL,            -- FHIR JSON 전체
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now(),
    PRIMARY KEY (resource_type, id)
);

CREATE INDEX IF NOT EXISTS idx_fhir_resource_url
    ON fhir.resource (resource_type, url);

CREATE INDEX IF NOT EXISTS idx_fhir_resource_name
    ON fhir.resource (resource_type, name);

CREATE INDEX IF NOT EXISTS idx_fhir_resource_status
    ON fhir.resource (resource_type, status);

-- IG 패키지 목록
CREATE TABLE IF NOT EXISTS fhir.package (
    id           VARCHAR(200) PRIMARY KEY,          -- {name}#{version}
    name         VARCHAR(200) NOT NULL,
    version      VARCHAR(50),
    description  TEXT,
    installed_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 리소스가 속한 IG 추적 (NULL = 직접 등록)
ALTER TABLE fhir.resource ADD COLUMN IF NOT EXISTS ig_id VARCHAR(200) REFERENCES fhir.package(id);
CREATE INDEX IF NOT EXISTS idx_fhir_resource_ig ON fhir.resource(ig_id);

-- 클라이언트 접근 및 작업 이력
CREATE TABLE IF NOT EXISTS fhir.access_log (
    id          BIGSERIAL PRIMARY KEY,
    ts          TIMESTAMP NOT NULL DEFAULT now(),
    method      VARCHAR(10),
    path        VARCHAR(500),
    query       VARCHAR(1000),
    client_ip   VARCHAR(50),
    user_agent  VARCHAR(300),
    status      INT,
    duration_ms INT
);
CREATE INDEX IF NOT EXISTS idx_fhir_access_log_ts ON fhir.access_log(ts DESC);
