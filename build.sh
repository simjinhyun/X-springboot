#!/bin/bash
set -e  # 에러 발생 시 즉시 종료

#!/bin/bash

# 1. 정보 추출
REV=$(git rev-parse --short HEAD)
# 수정된 파일이 있으면 -DIRTY 추가
DIRTY=$(git diff --quiet || echo "-DIRTY")
VER="${REV}${DIRTY}"
DATE=$(date "+%Y-%m-%d %H:%M:%S")

# 2. 원본 백업
TARGET_FILE="src/main/java/kr/co/semperpi/X.java"
cp $TARGET_FILE "${TARGET_FILE}.tmp"

# 3. sed로 치환 (홀더 규격 준수)
sed -i "s/###REVISION###/$REV/g" $TARGET_FILE
sed -i "s/###VERSION###/$VER/g" $TARGET_FILE
sed -i "s/###BUILD_DATE###/$DATE/g" $TARGET_FILE

# 4. 빌드 실행
./mvnw clean package

# 5. 원본 복구
mv "${TARGET_FILE}.tmp" $TARGET_FILE

echo "Build Complete: $VER ($DATE)"

# 빌드 성공 시 JAR 실행
java -jar target/mis.jar
