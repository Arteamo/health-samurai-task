FROM openjdk:17-oracle
COPY /target/health-samurai-task-0.1.0-standalone.jar .
ENV DB_PORT=5432
ENV DB_HOST="localhost"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "health-samurai-task-0.1.0-standalone.jar"]
