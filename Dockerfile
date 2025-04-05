# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app
ARG JAR_FILE=api/build/libs/api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/app.jar

# 엔트리포인트 스크립트와 wait-for-it 스크립트를 컨테이너로 복사
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
COPY wait-for-it.sh /usr/local/bin/wait-for-it.sh

# 실행 권한 부여
RUN chmod +x /usr/local/bin/entrypoint.sh /usr/local/bin/wait-for-it.sh

EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]