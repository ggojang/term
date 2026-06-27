#!/bin/bash
# 로컬(Mac): sdkman Java 25 사용. 원격 서버: /usr/lib/jvm/java-17-oracle
if [ -d "$HOME/.sdkman/candidates/java/current" ]; then
  export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
elif [ -d "/usr/lib/jvm/java-17-oracle" ]; then
  export JAVA_HOME=/usr/lib/jvm/java-17-oracle
fi
export PATH=$JAVA_HOME/bin:$PATH

export MAVEN_OPTS="\
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.util.concurrent=ALL-UNNAMED \
  --add-opens java.base/java.io=ALL-UNNAMED \
  --add-opens java.base/java.net=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/sun.nio.ch=ALL-UNNAMED"

cd "$(dirname "$0")"

# 프론트엔드 빌드
echo "[run.sh] Building frontend..."
cd frontend
export NODE_OPTIONS=--openssl-legacy-provider
npm run build
if [ $? -ne 0 ]; then
  echo "[run.sh] Frontend build failed. Aborting."
  exit 1
fi
cd ..

echo "[run.sh] Starting Spring server..."
mvn tomcat7:run -P run "$@"
