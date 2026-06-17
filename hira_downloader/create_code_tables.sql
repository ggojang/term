SET search_path = term;

CREATE TABLE IF NOT EXISTS hira_행위_code (
    수가코드        TEXT PRIMARY KEY,
    적용일자        DATE,
    분류번호        TEXT,
    한글명          TEXT,
    영문명          TEXT,
    구분            TEXT,
    수술여부        TEXT,
    의원단가        NUMERIC,
    병원단가        NUMERIC,
    상대가치점수    NUMERIC,
    시트구분        TEXT,
    장구분          TEXT,
    절구분          TEXT,
    세분류          TEXT,
    산정명칭        TEXT
);

CREATE INDEX IF NOT EXISTS idx_hira_행위_시트 ON hira_행위_code (시트구분);
CREATE INDEX IF NOT EXISTS idx_hira_행위_장절 ON hira_행위_code (장구분, 절구분);
CREATE INDEX IF NOT EXISTS idx_hira_행위_search ON hira_행위_code USING gin(
    to_tsvector('simple', coalesce(수가코드,'') || ' ' || coalesce(한글명,'') || ' ' || coalesce(영문명,''))
);

CREATE TABLE IF NOT EXISTS hira_약제_code (
    제품코드        TEXT,
    적용시작일자    DATE,
    급여기준        TEXT,
    상한가          NUMERIC,
    투여경로        TEXT,
    제품명          TEXT,
    규격            TEXT,
    단위            TEXT,
    업체명          TEXT,
    분류번호        TEXT,
    주성분코드      TEXT,
    전문일반        TEXT,
    PRIMARY KEY (제품코드, 적용시작일자)
);

CREATE INDEX IF NOT EXISTS idx_hira_약제_분류 ON hira_약제_code (분류번호);
CREATE INDEX IF NOT EXISTS idx_hira_약제_search ON hira_약제_code USING gin(
    to_tsvector('simple', coalesce(제품코드,'') || ' ' || coalesce(제품명,'') || ' ' || coalesce(주성분코드,''))
);

CREATE TABLE IF NOT EXISTS hira_치료재료_code (
    코드            TEXT PRIMARY KEY,
    최초등재일      DATE,
    적용일자        DATE,
    종료일자        DATE,
    중분류          TEXT,
    중분류코드      TEXT,
    품명            TEXT,
    규격            TEXT,
    단위            TEXT,
    상한금액        NUMERIC,
    제조회사        TEXT,
    재질            TEXT,
    급여구분        TEXT
);

CREATE INDEX IF NOT EXISTS idx_hira_치료재료_중분류 ON hira_치료재료_code (중분류코드);
CREATE INDEX IF NOT EXISTS idx_hira_치료재료_search ON hira_치료재료_code USING gin(
    to_tsvector('simple', coalesce(코드,'') || ' ' || coalesce(품명,'') || ' ' || coalesce(중분류,''))
);
