FROM openjdk:11-jdk-slim
ADD target/score-rank-0.0.1-SNAPSHOT.jar score-rank-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/score-rank-0.0.1-SNAPSHOT.jar", "--load.initialdata=true"]