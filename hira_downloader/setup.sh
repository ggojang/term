#!/bin/bash
# HIRA 다운로더 환경 설정 스크립트
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== HIRA 다운로더 설치 ==="

# Python 버전 확인 (3.9+ 사용)
PYTHON=$(which python3.11 2>/dev/null || which python3 2>/dev/null)
echo "Python: $($PYTHON --version)"

# pip 패키지 설치
echo "패키지 설치 중..."
$PYTHON -m pip install psycopg2-binary requests --quiet

# 다운로드 디렉토리 생성
mkdir -p "$SCRIPT_DIR/../release_files/hira_incoming"
echo "다운로드 폴더: $SCRIPT_DIR/../release_files/hira_incoming"

# cron 등록 안내
CRON_CMD="0 9 * * 1 HIRA_SLACK_WEBHOOK='YOUR_WEBHOOK_URL' $PYTHON $SCRIPT_DIR/hira_download.py >> $SCRIPT_DIR/hira_download.log 2>&1"
echo ""
echo "=== cron 등록 방법 ==="
echo "crontab -e 실행 후 아래 줄 추가:"
echo ""
echo "$CRON_CMD"
echo ""

# 현재 crontab에 자동 등록 여부 묻기
read -p "crontab에 자동 등록할까요? (y/N): " REPLY
if [[ "$REPLY" =~ ^[Yy]$ ]]; then
    # 기존 hira_download 항목 제거 후 재등록
    (crontab -l 2>/dev/null | grep -v "hira_download"; echo "$CRON_CMD") | crontab -
    echo "crontab 등록 완료. 'crontab -l'로 확인하세요."
fi

echo "=== 설치 완료 ==="
echo "수동 실행: HIRA_SLACK_WEBHOOK='...' $PYTHON $SCRIPT_DIR/hira_download.py"
