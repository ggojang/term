# STOM Browser — DB 적재 및 빌드 가이드

## 개요

| 항목 | 내용 |
|------|------|
| 백엔드 | Java 17, Spring 4.2.6, Maven, Tomcat7 embedded |
| 프론트엔드 | React 16, Node.js 18 |
| DB | PostgreSQL 15, DB=`term`, user=`postgres`, pw=`julab123!` |
| 백엔드 포트 | 8088 |
| 원격 서버 | 115.68.110.23 (`~/services/term`) |

---

## 1. 사전 준비 (원격 서버)

### Java 17
```bash
# 설치 확인
java -version   # openjdk 17 또는 oracle jdk 17
# 경로: /usr/lib/jvm/java-17-oracle
```

### Maven
```bash
mvn -version    # Apache Maven 3.8+
# 경로: /opt/apache-maven-3.8.6
```

### PostgreSQL
```bash
# psql 경로
export PATH=$PATH:/home/infoclinic/bin
psql -U postgres -d term -c "SELECT version();"
```

### Python 3 (HIRA 코드 적재용)
```bash
python3 --version        # 3.6+
pip3 install --user psycopg2-binary==2.8.6
```

---

## 2. 소스 배포

```
로컬 ~/github/term
    ↓ git push
github.com/ggojang/term
    ↓ git pull (원격 서버에서)
원격 ~/services/term
```

```bash
# 원격 서버에서
cd ~/services/term
git pull
```

---

## 3. DB 적재

### 3-1. SNOMED CT / LOINC / KCD-9 (통합 임포터)

```bash
cd ~/services/term

# release_files/ 아래에 다음 파일들이 있어야 함:
#   SnomedCT_InternationalRF2_PRODUCTION_*.zip  (코어)
#   SnomedCT_*_PRODUCTION_*.zip                  (익스텐션, 여러 개)
#   loinc/Loinc.csv, loinc/LoincHierarchy.csv   (LOINC)
#   icd10/                                        (KCD-9, ICD10_CLASS)

./PostgreSQLImporter.sh
# 또는 경로 지정:
./PostgreSQLImporter.sh /path/to/release_files
```

적재 순서:
1. SNOMED CT 코어 테이블 (스키마 재생성)
2. SNOMED CT 익스텐션 (append)
3. Transitive Closure 생성
4. REFERENCESET_ACTIVE (`refset_active_pg.sh`)
5. MRCM CONSTRAINTS (`mrcm_pg.sh`)
6. LOINC 테이블
7. KCD-9 (`icd10.icd10_class` 한글, `icd10.kcd9_morph`)

### 3-2. HIRA 코드 테이블 생성

```bash
cd ~/services/term

# 테이블 생성 (최초 1회)
psql -U postgres -d term -f hira_downloader/create_code_tables.sql
```

생성되는 테이블:
- `term.hira_행위_code` — 행위(수가) 코드
- `term.hira_약제_code` — 약제 코드 (9자리)
- `term.hira_치료재료_code` — 치료재료 코드
- `term.hira_atc_master` — ATC 코드
- `term.kdcode` — KPIS KD코드 (표준코드목록)

### 3-3. HIRA 코드 다운로드 및 적재

```bash
cd ~/services/term

# HIRA 파일 다운로드 (release_files/hira_incoming/ 에 저장)
python3 hira_downloader/hira_download.py

# 또는 수동으로 파일 복사 후 적재
# release_files/hira_incoming/StdCdListTitle.csv
# release_files/hira_incoming/StdCdList.csv
```

```bash
# KD코드 적재 (~530,223건)
python3 hira_downloader/load_kdcode.py

# HIRA 행위/약제/치료재료/ATC 적재
python3 hira_downloader/load_hira_codes.py
python3 hira_downloader/load_atc_master.py
```

### 3-4. FHIR ValueSet 등록

서버 기동 후 아래 ValueSet을 등록해야 `$expand` 동작:

```bash
BASE="http://localhost:8088/fhir"

# KCD-8 (icd10.icd10_class 연결, KCD-9와 동일 DB)
curl -X POST "$BASE/ValueSet" -H "Content-Type: application/json" -d '{
  "resourceType":"ValueSet","url":"http://www.hl7korea.or.kr/ValueSet/kcd-8",
  "name":"KCD8","status":"active",
  "compose":{"include":[{"system":"http://www.hl7korea.or.kr/CodeSystem/kostat-kcd-8"}]}
}'

# KCD-9
curl -X POST "$BASE/ValueSet" -H "Content-Type: application/json" -d '{
  "resourceType":"ValueSet","url":"http://www.hl7korea.or.kr/ValueSet/kcd-9",
  "name":"KCD9","status":"active",
  "compose":{"include":[{"system":"http://www.hl7korea.or.kr/CodeSystem/kostat-kcd-9"}]}
}'

# HIRA 행위
curl -X POST "$BASE/ValueSet" -H "Content-Type: application/json" -d '{
  "resourceType":"ValueSet","url":"http://www.hl7korea.or.kr/ValueSet/hira-procedure-codes",
  "name":"HiraProcedure","status":"active",
  "compose":{"include":[{"system":"http://www.hl7korea.or.kr/CodeSystem/hira-edi-procedure"}]}
}'

# KPIS KD코드
curl -X POST "$BASE/ValueSet" -H "Content-Type: application/json" -d '{
  "resourceType":"ValueSet","url":"http://www.hl7korea.or.kr/ValueSet/kpis-kdcode",
  "name":"KpisKdcode","status":"active",
  "compose":{"include":[{"system":"http://www.hl7korea.or.kr/CodeSystem/kpis-kdcode"}]}
}'
```

---

## 4. 프론트엔드 빌드

```bash
cd ~/services/term/frontend

# Node.js 18 필요 (nvm 사용 시)
# nvm use 18

npm install
npm run build
# 또는: yarn build

# 빌드 결과물이 ../src/main/webapp/ 에 자동 복사됨
```

> Node.js 버전 주의: Node 17 이상에서 `NODE_OPTIONS=--openssl-legacy-provider` 불필요 (Node 10/18 사용 권장)

---

## 5. 백엔드 빌드 및 실행

### 기존 프로세스 종료

```bash
# Java 프로세스 전체 종료
kill $(ps aux | grep 'tomcat7:run' | grep -v grep | awk '{print $2}')
# 또는 PID 직접 지정
kill <PID>
```

### 서버 시작

```bash
cd ~/services/term

# 포그라운드 실행 (로그 직접 확인)
./run.sh

# 백그라운드 실행
nohup ./run.sh > run.out 2>&1 &

# 로그 확인
tail -f run.out
```

`run.sh` 내용:
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-oracle   # 원격 서버
export MAVEN_OPTS="--add-opens java.base/..."
mvn tomcat7:run -P run
```

---

## 6. Apache 프록시 설정 (원격 서버)

`/etc/apache2/sites-enabled/000-default.conf` — `stom.infoclinic.co` 가상호스트:

```apache
<VirtualHost *:80>
    ServerName stom.infoclinic.co
    ProxyPass /stom/ http://localhost:8088/stom/
    ProxyPassReverse /stom/ http://localhost:8088/stom/
    ProxyPass / http://localhost:8088/
    ProxyPassReverse / http://localhost:8088/
</VirtualHost>
```

설정 변경 후:
```bash
echo "openehr123!" | sudo -S service apache2 reload
```

---

## 7. 접속 확인

| 환경 | URL |
|------|-----|
| 로컬 | `http://localhost:8088/` |
| 원격 | `http://stom.infoclinic.co/` |

### FHIR API 헬스체크

```bash
BASE="http://stom.infoclinic.co/fhir"

# KCD-8/9 조회
curl "$BASE/CodeSystem/\$lookup?system=http://www.hl7korea.or.kr/CodeSystem/kostat-kcd-8&code=A00"

# KD코드 조회
curl "$BASE/CodeSystem/\$lookup?system=http://www.hl7korea.or.kr/CodeSystem/kpis-kdcode&code=8800500000102"

# HIRA 행위 조회
curl "$BASE/CodeSystem/\$lookup?system=http://www.hl7korea.or.kr/CodeSystem/hira-edi-procedure&code=AA100"

# ValueSet expand (KCD-8)
curl "$BASE/ValueSet/\$expand?url=http://www.hl7korea.or.kr/ValueSet/kcd-8&count=5"
```

---

## 8. DB 연결 정보

| 항목 | 값 |
|------|-----|
| Host | localhost:5432 |
| DB | term |
| User | postgres |
| Password | julab123! |
| SNOMED/LOINC/HIRA 스키마 | term |
| KCD-9 스키마 | icd10 |
| FHIR 리소스 스키마 | fhir |

> KCD-8과 KCD-9 모두 `icd10.icd10_class` 테이블을 공유합니다.
