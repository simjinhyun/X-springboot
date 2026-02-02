#!/bin/bash

REV=$(git rev-parse --short HEAD)
VER=$(git describe --tags --dirty --always)
DATE=$(date "+%Y-%m-%d %H:%M:%S")
TARGET="src/main/java/kr/co/semperpi/X.java"

# 1. 백업
cp "$TARGET" "$TARGET.bak"

# 2. 치환
sed -i "s/###REVISION###/$REV/g" "$TARGET"
sed -i "s/###VERSION###/$VER/g" "$TARGET"
sed -i "s/###BUILD_DATE###/$DATE/g" "$TARGET"

# 3. 빌드 (실패해도 다음으로 넘어감)
mvn clean package

# 4. 무조건 복구
mv "$TARGET.bak" "$TARGET"

# 5. 결과 확인 및 실행
if [ -f "target/mis.jar" ]; then
    echo "Build Success: $VER"
    java -jar target/mis-exec.jar
else
    echo "Build Failed!"
    exit 1
fi