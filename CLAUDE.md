# CLAUDE.md

## 기본정보

STOM Browser의 로컬 root는 ~/github/term, 프론트엔드 root는 ~/github/term/frontend, 로컬 연결 URL은 http://localhost:8088, 실행은 ./run.sh

FHIR Endpoint URL은 http://localhost:8088/fhir

## 작업 이력 참조
- 이전 서버에서 했던 작업 요약은 `work_history.md`에 있으니 참고할 것
- 절대 이전 서비스가 작업 중인 소스에  나타나지 않도록 해줘 (예. api.infoclinic.co, MySQL SQL, Elasticsearch 등)
- 앞으로 할 작업의 요약은 `~/github/term/history.md`에 수실로 기록하고 작업할 때마다 참고할 것

## 배포 흐름

```
작업 내용 소스에 적용 후
로컬 ~/github/term
    ↓ push
github.com/ggojang/term
    ↓ pull
원격 서버 115.68.110.23 (infoclinic/openehr123!) ~/services/term
빌드 후 프로세스 종료, ./run/sh

```

## 주의사항
- github.com/ggojang/term을 통하지 않고 로컬 소스 기반으로 원격 소스가 바로 변경되는 일이 없도록 할 것
- 원격 서버에 직접 파일을 올리거나 수정하지 말 것 (반드시 GitHub 경유)
