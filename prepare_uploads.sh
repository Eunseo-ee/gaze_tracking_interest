#!/bin/bash

UPLOAD_TARGET="src/main/resources/static/uploads"
UPLOAD_SOURCE="static_uploads_source"

# 업로드 대상 폴더 초기화
mkdir -p "$UPLOAD_TARGET"
rm -f "$UPLOAD_TARGET"/*

# 복사
cp "$UPLOAD_SOURCE"/*.mp4 "$UPLOAD_TARGET"/ 2>/dev/null
cp "$UPLOAD_SOURCE"/*.csv "$UPLOAD_TARGET"/ 2>/dev/null

echo "📁 Uploads 폴더가 새로 복사되었습니다."
