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

## HIRA 코드자료 자동 다운로더 (2026-06-17)

- `hira_downloader/hira_download.py` — Playwright 기반 크롤링 + 다운로드 메인 스크립트
- `hira_downloader/setup.sh` — 설치 및 cron 등록 스크립트
- `hira_downloader/create_table.sql` — DB 테이블 DDL (term.hira_downloads)
- 다운로드 경로: `release_files/hira_incoming/`
- cron: 매일 09:00, Slack webhook 알림 지원 (HIRA_SLACK_WEBHOOK 환경변수)
- **초기 실행 전**: `hira_downloader/setup.sh` 실행 후 HIRA_SLACK_WEBHOOK 설정 필요

## 로컬 DB 미적용 항목 (추후 LOINC 버전 업데이트 시 적용 예정)

- `loinc.HIERARCHY` DESCENDANT_COUNT 재계산 (PATH 기반, 293,674행)
- `loinc.HIERARCHY_LG` DESCENDANT_COUNT 재계산 (43,767행)
- 현재 로컬에서 트리 descendant count가 틀리게 표시될 수 있으나 기능 동작에는 무관
