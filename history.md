# Term 프로젝트 작업 히스토리

## 프로젝트 구조

- **로컬**: `~/github/term` (macOS, seungjong.yu)
- **GitHub**: `github.com/ggojang/term` (로컬 → push)
- **원격 서버**: `115.68.110.23` (id: infoclinic / pw: openeher123!) `~/github/term` (GitHub → pull)
- **흐름**: 로컬 개발 → GitHub push → 원격 서버 pull → 원격에서 실행

## 기술 스택

- **백엔드**: Java 1.8 타겟, Spring 4.2.6, Maven, Tomcat7 embedded (`mvn tomcat7:run`)
- **프론트엔드**: React 16, node-sass, build 결과는 `src/main/webapp/stom/`에 위치
- **DB**: PostgreSQL localhost:5432, DB=term, schema=term, user=postgres, pw=julab123!

## 로컬 환경 현황 (2026-06-17 기준)

| 항목 | 상태 | 비고 |
|------|------|------|
| Java | ✅ OpenJDK 25 (Zulu) | run.sh는 Java 17 Oracle 경로 사용 → 로컬에서는 별도 설정 필요 |
| Maven | ✅ 3.9.14 | |
| Node.js | ❌ 미설치 | 프론트엔드 빌드에 필요 |
| PostgreSQL | ❌ 미설치 (psql 없음) | DB 세팅 필요 |
| 프론트엔드 빌드 | ❌ stom/ 디렉토리 없음 | `npm run build` 필요 |

## 로컬 빌드 세팅 필요 작업

1. **Node.js 설치** → `brew install node@18` (node-sass 호환)
2. **PostgreSQL 설치** → `brew install postgresql@15` + DB/스키마 생성
3. **프론트엔드 빌드** → `cd frontend && npm install && npm run build`
4. **백엔드 실행** → Java 25에서 MAVEN_OPTS 조정 후 `mvn tomcat7:run -P run`

## 로컬 실행 명령

```bash
# Node PATH 등록 (터미널 새로 열 때마다 또는 ~/.zshrc에 추가)
export PATH="/opt/homebrew/opt/node@18/bin:$PATH"

# 백엔드 실행 (포트 8088)
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED"
cd ~/github/term && mvn tomcat7:run -P run

# 프론트엔드 재빌드 (소스 변경 시)
cd ~/github/term/frontend && npm run build
cp -r build ../src/main/webapp/stom
```

접속: http://localhost:8088/stom/

## 작업 로그

### 2026-06-17
- 원격 서버(115.68.110.23)에서 작성된 소스를 GitHub를 통해 로컬에 복사 완료
- 로컬 환경 분석: Node.js 미설치, PostgreSQL 18 이미 설치·실행 중(term DB 포함) 확인
- Node.js 18 설치 (`brew install node@18`)
- `npm install --ignore-scripts` 로 프론트엔드 의존성 설치 (fibers 네이티브 빌드 우회)
- 프론트엔드 빌드 후 `src/main/webapp/stom/` 에 복사
- `mvn tomcat7:run -P run` 으로 백엔드 기동 성공
- http://localhost:8088/stom/ HTTP 200 확인
- term_backend (포트 8080) 종료 후 로컬 서버(8088)만 운영 중
- 원격 서버 ~/work_history.md 내용 확인 및 로컬 history.md에 통합

## FHIR Terminology Server 구현 (원격 서버 작업, 로컬 미병합)

> `src/main/java/co/infoclinic/term/fhir/` — Spring MVC 기반 FHIR R4 Terminology Server

### 구조
```
fhir/
├── api/           FhirApi.java          — 엔드포인트 상수 (BASE: /stom/fhir)
├── controller/
│   ├── CapabilityStatementController   — GET /stom/fhir/metadata
│   ├── FhirCodeSystemController        — CodeSystem CRUD + $lookup / $validate-code / $subsumes
│   ├── FhirValueSetController          — ValueSet CRUD + $expand / $validate-code
│   ├── FhirConceptMapController        — ConceptMap CRUD + $translate
│   ├── FhirNamingSystemController      — NamingSystem CRUD
│   └── FhirPackageController           — $install-package
├── service/       (+ impl/)            — 각 리소스별 서비스 레이어
├── model/
│   ├── entity/    FhirResource, FhirResourceId
│   └── dto/
└── repository/
```

### 제공 엔드포인트 요약
| 리소스 | 경로 | 주요 Operation |
|--------|------|----------------|
| CapabilityStatement | `/stom/fhir/metadata` | GET |
| CodeSystem | `/stom/fhir/CodeSystem[/{id}]` | CRUD + `$lookup`, `$validate-code`, `$subsumes` |
| ValueSet | `/stom/fhir/ValueSet[/{id}]` | CRUD + `$expand`, `$validate-code` |
| ConceptMap | `/stom/fhir/ConceptMap[/{id}]` | CRUD + `$translate` |
| NamingSystem | `/stom/fhir/NamingSystem[/{id}]` | CRUD |
| Package | `/stom/fhir/$install-package` | POST |

### 관련 변경 파일
- `pom.xml` — FHIR 의존성 추가 (HAPI FHIR 등 예상)
- `src/main/webapp/WEB-INF/spring/mvc.xml` — FHIR 컨트롤러 컴포넌트 스캔 추가
- `src/main/resources/log4jdbc.log4j2.properties` — 로깅 설정

## 요양급여청구코드 브라우저 (2026-06-17)

### 개요
HIRA 청구관련기준(마스터파일) 3종을 DB에 적재하고, STOM Browser에 새 탭 "요양급여청구코드" 추가.

### DB 적재
- `hira_downloader/create_code_tables.sql` — 3개 테이블 생성
  - `term.hira_행위_code` (419,025행) — 수가코드, 한글명, 영문명, 단가
  - `term.hira_약제_code` (59,110행) — 제품코드, 제품명, 상한가, 약효분류번호
  - `term.hira_치료재료_code` (29,004행) — 코드, 품명, 중분류, 상한금액
- `hira_downloader/load_hira_codes.py` — xlsb/xlsx → PostgreSQL 적재 스크립트
  - pyxlsb (행위 xlsb), openpyxl (약제/치료재료 xlsx) 사용

### Backend
- `src/main/java/co/infoclinic/term/hira/controller/HiraController.java` — REST API
- `src/main/java/co/infoclinic/term/hira/service/HiraService.java` — Native Query 서비스
- API 패턴: `/hira/{카테고리}/tree`, `/hira/{카테고리}/search`, `/hira/{카테고리}/{code}`

### Frontend
- `frontend/src/hira/layout.js` — 3:9 Grid 레이아웃
- `frontend/src/hira/left.js` — 행위|약제|치료재료 탭 + 트리 + 검색
- `frontend/src/hira/main.js` — 코드 상세 패널 (단가, 이력 포함)
- `frontend/src/App.js` — 탭 5번 "요양급여청구코드" 추가

### 계층 구조
- 행위: 시트구분(의치과_급여 등) → 장구분 → 절구분 → 수가코드
- 약제: 분류번호(약효분류 3자리) → 제품코드
- 치료재료: 코드 첫글자(A/B/C...) → 중분류코드 → 코드

---

## HIRA 코드자료 자동 다운로더 (2026-06-17)

- `hira_downloader/hira_download.py` — requests 기반 HIRA 포털 크롤링 + 다운로드 + DB 적재 자동 연결
- `hira_downloader/setup.sh` — 설치 및 cron 등록 스크립트
- `hira_downloader/create_table.sql` — DB 테이블 DDL (term.hira_downloads)
- 다운로드 경로: `release_files/hira_incoming/`
- cron: 매주 월요일 09:00, Slack webhook 알림 지원 (HIRA_SLACK_WEBHOOK 환경변수)
- **자동 적재 흐름**: 새 파일 다운로드 감지 → `load_hira_codes.py` 자동 실행 → Slack 알림
- **중복 방지**: `hira_downloads` 테이블의 `post_no`로 이미 처리된 게시글 건너뜀
- **초기 실행 전**: `hira_downloader/setup.sh` 실행 후 HIRA_SLACK_WEBHOOK 설정 필요

---

## HIRA 트리 구조 및 검색 개선 (2026-06-17)

### 행위 트리
- 5단계: 시트구분 → 장(01장 기본진료료 등 타이틀 포함) → 절 → 세분류 → 분류번호 → 청구코드
- 장·절 타이틀 정적 Map으로 표시 (예: "01장 기본진료료")
- 검색에 분류번호 포함
- 리프 노드 레이블 중복 코드 제거 (코드는 codeTag로만 표시)

### 약제 트리 (ATC 계층)
- ATC 그룹 레이블: `code\t한글명\tename` 탭 구분으로 전달
- 프론트엔드에서 한글명은 일반 크기, 영문명은 작은 회색 글씨로 별도 렌더링
- 리프 노드 레이블 중복 코드 제거

### 치료재료 트리
- 3단계: 엑셀시트명(급여_품목 등) → 중분류(코드+명칭) → 품명(리프)
- `hira_치료재료_code` 테이블에 `시트명` 컬럼 추가, 4개 시트 적재
- 검색: 중분류(group)와 품목(leaf) UNION, type 필드로 구분 렌더링
- 중분류 검색 결과는 파란색 ▸ 중분류 뱃지, 클릭 불가

---

## FHIR Terminology Server 기능 확장 (2026-06-17)

### BASE path 수정
- `FhirApi.BASE`: `/stom/fhir` → `/fhir` (Spring MVC context path 중복 버그 수정)
- `CapabilityStatementController`: 하드코딩 `/stom/fhir` → `request.getContextPath()` 기반으로 수정

### HIRA EDI CodeSystem 브리징
`FhirCodeSystemService`에 HIRA 3종 `$lookup` / `$validate-code` 추가:

| URL | DB 테이블 | 조회 컬럼 |
|-----|-----------|-----------|
| `http://www.hl7korea.or.kr/CodeSystem/hira-edi-procedure` | `hira_행위_code` | 수가코드 → 한글명, 영문명(designation) |
| `http://www.hl7korea.or.kr/CodeSystem/hira-edi-medication` | `hira_약제_code` | 제품코드 → 제품명 |
| `http://www.hl7korea.or.kr/CodeSystem/hira-edi-material` | `hira_치료재료_code` | 코드 → 품명 |

### ValueSet/$validate-code 구현
- `system`만 있을 때 → `CodeSystem/$validate-code`로 위임 (SNOMED, LOINC, KCD-9, HIRA 3종)
- ValueSet URL/ID 있을 때 → `$expand` 후 코드 포함 여부 확인
- GET / POST 모두 지원

### SNOMED CT Implicit ValueSet 지원
- `ConstraintController.evaluateECLPublic()` 추출 (기존 ECL2 실행기 재사용)
- 지원 URL 패턴:
  - `http://snomed.info/sct?fhir_vs=ecl/<<73211009` — ECL 표현식 (ECL2 전체 문법)
  - `http://snomed.info/sct?fhir_vs=refset/450976002` — Reference Set
- `$expand` 및 `$validate-code` 모두 implicit ValueSet URL 처리
- offset / count / filter 파라미터 지원

### FHIR root 페이지
- `GET /stom/fhir` → STOM Browser FHIR Endpoint 안내 HTML (한글 UTF-8)

---

## ATC 마스터 수집 스크립트 (2026-06-17)

- `hira_downloader/load_atc_master.py` — 약학정보원(health.kr) ATC 코드 전체 수집 및 DB 적재
- **출처**: https://health.kr/searchDrug/ATCcode.renewal.asp
- **API**: `ajax_atccode.asp?select_mode={1~5}&req_word={상위코드}` (JSON)
- **수집 방식**: BFS 탐색 — 1자리 → 3자리 → 4자리 → 5자리 → 7자리 전체 계층
- **적재 테이블**: `term.hira_atc_master` (atc_code PK, atc_name 영문, atc_hname 한글, vol 일일유지용량)
- **결과**: 1322건 upsert (중복 제거 포함)
- **실행**: `python3 hira_downloader/load_atc_master.py` (약 1분 소요)
- `create_code_tables.sql`에 `hira_atc_master` DDL 추가 (vol 컬럼 포함)
- 기존 DB에 vol 컬럼 없을 경우: `ALTER TABLE term.hira_atc_master ADD COLUMN IF NOT EXISTS vol VARCHAR(100);`

---

## FHIR Terminology Server DB 스키마 및 기타 수정 (2026-06-17)

### FHIR resource 테이블 DDL 추가
- `fhir/create_fhir_schema.sql` — 신규 추가 (기존 소스에 없었음)
  - `fhir` 스키마 생성
  - `fhir.resource` 테이블: resource_type, id, url, version, name, title, status, content, created_at, updated_at
  - PK: (resource_type, id), 인덱스: url / name / status

### 신규 서버 설치 순서
```bash
psql -U postgres -d term -f fhir/create_fhir_schema.sql        # FHIR 스키마
psql -U postgres -d term -f hira_downloader/create_code_tables.sql  # HIRA 코드 테이블
python3 hira_downloader/load_hira_codes.py                      # HIRA 코드 적재
python3 hira_downloader/load_atc_master.py                      # ATC 마스터 적재
```

### NamingSystem 수정
- `FhirNamingSystemController`: fullUrl 하드코딩 `/stom/fhir` → `FhirApi.BASE` 사용
- `GET /NamingSystem/$preferred-id?id={value}&type={oid|uri|...}` 구현
- `FhirApi`: `NAMING_SYSTEM_PREFERRED` 상수 추가

### CapabilityStatement 수정
- NamingSystem 리소스 선언 추가 (read/create/update/delete/search-type + `$preferred-id`)
- implementation description에 HIRA EDI 추가

### LOINC implicit ValueSet 구현 (2026-06-17)
- `FhirValueSetService.expandLoincImplicit()` 구현
- 지원 URL 패턴:
  - `http://loinc.org/vs` → `loinc.loinc` 전체 (filter, offset, count 지원)
  - `http://loinc.org/vs/LL{id}` → `loinc.la` Answer List (LL로 시작)
  - `http://loinc.org/vs/{LOINC-code}` → `loinc.la_link` + `loinc.la` 조인으로 해당 코드의 Answer List
  - `http://loinc.org/vs/loinc-{CLASS}` → `loinc.loinc WHERE class_name = ?`
- `ValueSet/$expand?url=http://loinc.org/vs/LL...` 등으로 호출 가능
- `expand()` 라우팅은 이전 세션에서 이미 추가됨

---

## UI 개선 및 기능 수정 (2026-06-22)

### Context path 변경
- `pom.xml` 로컬 실행 프로파일 `<path>`: `/stom` → `/`
- `frontend/package.json` `"homepage"`: `"/stom"` → `"/"`
- 접속: `http://localhost:8088/stom/` → `http://localhost:8088/`
- 프론트엔드 빌드 출력 경로: `src/main/webapp/stom/` → `src/main/webapp/`
- `frontend/package.json` build 스크립트 변경: `BUILD_PATH`로 직접 복사 → `build/` 빌드 후 `cp -r build/. ../src/main/webapp/` 방식으로 수정 (Node 18에서 BUILD_PATH 미동작 대응)

### SNOMED CT 검색 개선
- 다중단어 부분 검색 수정 (`hypers cond` → `hypersensitivity condition` 검색 가능)
  - `SearchServiceImpl.buildTermWhere()`: 단어별 `term ILIKE ?` AND 조건으로 분리
  - `setTermParam()`: 토큰별 `%token%` 바인딩
  - `nextParamIdx()`: 2글자 이상 토큰 수 기반 offset 계산
- 검색창 placeholder: `"Search by term, SCTID, FSN..."` → `"At least 2 characters"`
- 검색 아이콘(돋보기) 제거, padding 조정

### SNOMED CT 검색결과 UI
- 2줄 표시: 1줄(노란 배지 + term), 2줄(FSN 회색) 복원
- semantic tag 색깔 배지 제거
- Parent/Children/Hierarchy 트리 배지 배경: `#999` → 노란 그라디언트 (`#f7edb5 → #f5e79e`)

### RefSet Viewer
- 트리에서 member가 존재하는 노드명 black bold 표시
  - 마운트 시 `/refsets/SNOMEDCT?release=itn&hasmbrs=true` 단일 API 호출 → memberSet 구성
  - 노드 렌더링 시 memberSet 포함 여부로 스타일 분기

### ECL AND ^ refset 쿼리 타임아웃 수정
- `ConstraintController.evaluateCompound()` AND 연산 최적화
  - `^` (memberOf) 파트와 계층(`<<`) 파트 분리
  - 계층 파트는 TC 기반으로 먼저 평가
  - refset 파트는 `findMemberIdsByRefsetIdAndEffectiveTime()` 단일 쿼리 → in-memory Set 필터
  - 기존 N+1 쿼리로 인한 타임아웃 해소
- `LatestRefsetMemberRepository` 주입 추가

### LOINC / Mapping Support 검색창
- LOINC: `InputLabel` 제거 → `TextField placeholder` 방식으로 변경
- Mapping Support: label `"At least 2 more characters"` → placeholder `"At least 2 characters"`

### Swagger UI 확장 (57 → 111 엔드포인트)
- `SwaggerConfig`: `withMethodAnnotation(ApiOperation.class)` → `basePackage("co.infoclinic.term")`
- 모든 컨트롤러에 `@Api`, `@ApiOperation` 추가
  - FHIR: CapabilityStatement, CodeSystem, ValueSet, ConceptMap, NamingSystem, Package
  - HIRA: HiraController
  - SNOMED: TcController, MapSearchController
- Tag 정의 추가: `V-01~06 FHIR`, `IV-01 HIRA`, `VI-01 Map`
- API 버전: `"2.0"`, 설명: "SNOMED CT · LOINC · ICD-10/KCD · HIRA · FHIR R4 통합 용어 서비스 API"

### KCD-9 확장 코드 태극기 표시
- 기존: `🇰🇷` 이모지 (Windows/Linux에서 "KR" 텍스트로 렌더링되는 문제)
- 변경: 공식 태극기 SVG 이미지 (`/flag-kr.svg`) 사용
  - `frontend/public/flag-kr.svg` 추가 (위키미디어 공식 SVG)
  - `frontend/src/icd10/left.js`: `<img src="/flag-kr.svg" height="1em">` 로 교체 (트리 + 검색결과)
  - 빌드 스크립트에 `cp public/flag-kr.svg ../src/main/webapp/flag-kr.svg` 자동 복사 추가

---

---

## FHIR Terminology Server 기능 확장 (2026-06-23)

### KR-Core Terminology 전체 지원

#### FHIR CodeSystem DB 연결

| URL | DB 테이블 | 비고 |
|-----|-----------|------|
| `http://www.hl7korea.or.kr/CodeSystem/kostat-kcd-8` | `icd10.icd10_class` | 버전 무관 동일 조회 |
| `http://www.hl7korea.or.kr/CodeSystem/kostat-kcd-9` | `icd10.icd10_class` | 버전 무관 동일 조회 |
| `http://www.whocc.no/atc` | `term.hira_atc_master` | atc_code, atc_name, atc_hname |
| `http://www.hl7korea.or.kr/CodeSystem/kpis-kdcode` | `term.kdcode` | 표준코드, 표준코드명칭, 급여비급여구분, 상한가 |

- `FhirCodeSystemService`: 위 4개 URL에 대해 `$lookup`, `$validate-code` 분기 추가
- `FhirCodeSystemService`: `buildKcd8Stub()`, `buildAtcStub()`, `buildKpisKdcodeStub()` 메서드 추가

#### FHIR ValueSet $expand 전체 DB 조회 분기 추가

`FhirValueSetService.expandInclude()`에 시스템별 전체 조회 분기 추가:

| CodeSystem URL | 메서드 | DB 테이블 |
|---|---|---|
| `http://www.whocc.no/atc` | `expandAtcAll()` | `term.hira_atc_master` |
| `http://www.hl7korea.or.kr/CodeSystem/kpis-kdcode` | `expandKdcodeAll()` | `term.kdcode` |
| `http://www.hl7korea.or.kr/CodeSystem/hira-edi-procedure` | `expandHiraEdiProcedureAll()` | `term.hira_행위_code` |
| `http://www.hl7korea.or.kr/CodeSystem/hira-edi-medication` | `expandHiraEdiMedicationAll()` | `term.kdcode` |
| `http://www.hl7korea.or.kr/CodeSystem/hira-edi-material` | `expandHiraEdiMaterialAll()` | `term.hira_치료재료_code` |
| SNOMED CT | `expandSnomedAll()` | `term.concept` + `term.description` |
| LOINC | `expandLoincAll()` | `loinc.loinc` |
| KCD-8/9 | `expandKcdAll()` | `icd10.icd10_class` |

#### include.valueSet 참조 지원
- `FhirValueSetService.expandReferencedValueSet()` 추가
- `compose.include`에 `valueSet` 참조가 있는 경우 재귀적으로 expand
- 적용 ValueSet: `krcore-diagnostic-imaging-codes`, `krcore-laboratory-codes`, `krcore-pathology-codes`, `krcore-procedure-codes`

#### KR-Core ValueSet $expand 지원 현황 (전체 20개 ✅)

| ValueSet | total |
|---|---|
| krcore-kcd8-codes | 25,426 |
| krcore-snomed-ct-codes | 1,172,605 |
| krcore-loinc-codes | 109,325 |
| krcore-atc-codes | 1,322 |
| krcore-korea-drug-codes | 327,280 |
| krcore-edi-procedure-codes | 419,024 |
| krcore-edi-medication-codes | 327,280 |
| krcore-edi-material-codes | 32,062 |
| krcore-diagnostic-imaging-codes | EDI + LOINC 합산 |
| krcore-laboratory-codes | EDI + LOINC 합산 |
| krcore-pathology-codes | EDI + SNOMED 합산 |
| krcore-procedure-codes | EDI + SNOMED 합산 |
| 소규모 8종 (condition-category 등) | 저장된 CodeSystem 기반 |

### KPIS KD코드 표준코드목록 DB 적재

- **출처**: `release_files/hira_incoming/StdCdList.csv` (HIRA 표준코드목록 배포파일)
- **컬럼 정의**: `release_files/hira_incoming/StdCdListTitle.csv` (17개 컬럼)
- **적재 테이블**: `term.kdcode`
- **적재 건수**: 530,223건 (DISTINCT 표준코드 기준 327,280개)
- **신규 파일**:
  - `hira_downloader/create_code_tables.sql` — `term.kdcode` DDL 추가
  - `hira_downloader/load_kdcode.py` — StdCdList.csv → `term.kdcode` 적재 스크립트

#### 신규 서버 설치 순서 (업데이트)
```bash
psql -U postgres -d term -f fhir/create_fhir_schema.sql
psql -U postgres -d term -f hira_downloader/create_code_tables.sql
python3 hira_downloader/load_hira_codes.py
python3 hira_downloader/load_atc_master.py
python3 hira_downloader/load_kdcode.py    # StdCdList.csv 필요
```

---

## 로컬 DB 미적용 항목 (추후 LOINC 버전 업데이트 시 적용 예정)

- `loinc.HIERARCHY` DESCENDANT_COUNT 재계산 (PATH 기반, 293,674행)
- `loinc.HIERARCHY_LG` DESCENDANT_COUNT 재계산 (43,767행)
- 현재 로컬에서 트리 descendant count가 틀리게 표시될 수 있으나 기능 동작에는 무관
