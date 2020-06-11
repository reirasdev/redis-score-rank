![CD | Maven | Docker | AWS EC2](https://github.com/reirasdev/score-rank/workflows/CD%20%7C%20Maven%20%7C%20Docker%20%7C%20AWS%20EC2/badge.svg?branch=master)

## Pipa Score Rank
HTTP based mini game backend with Java, Spring Boot and Redis which registers score points for different users and returns user position and high score list.

### 1 - Starting your application:
1. You will need Maven and a running Redis Instance</br>
2. Clone this repository or download the project [here](https://github.com/reirasdev/score-rank/archive/master.zip)
3. At the project root, go to ./src/main/resources/application.properties
   - Point *redis.host* to your host running Redis
   - Point *redis.port* to the port listennig for Redis connection
4. At the project root, run the following instruction:
   - *mvn spring-boot:run*
5. Open your browser and go to:
   - http://localhost:8080/swagger-ui.html

### 2 - Running system with available data for tests
If you prefer, a instance of this system is already available at: </br>
http://ec2-18-228-59-179.sa-east-1.compute.amazonaws.com:8080/swagger-ui.html</br>
It is pointing to a Redis set with almost 30.000 scores registered.

### 3 - Run with Docker
Run Redis with [docker-compose-redis.yml](https://github.com/reirasdev/score-rank/blob/master/docker-compose-redis.yml)</br>
Run this app with [docker-compose.yml](https://github.com/reirasdev/score-rank/blob/master/docker-compose.yml)</br>
Images available at https://hub.docker.com/r/reirasdev/score-rank
