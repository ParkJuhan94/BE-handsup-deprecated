#!/bin/sh
# MySQL(hands-up-mysql:3306)이 준비될 때까지 최대 40초까지 대기
/usr/local/bin/wait-for-it.sh hands-up-mysql:3306 -t 40

# 준비되면 애플리케이션 실행
exec java \
  -Xlog:gc*:file=/logs/gc.log:time \
  -jar /app/app.jar