![CD | Maven | Docker | AWS EC2](https://github.com/reirasdev/score-rank/workflows/CD%20%7C%20Maven%20%7C%20Docker%20%7C%20AWS%20EC2/badge.svg?branch=master)

## Pipa Score Rank
HTTP based mini game backend with Java and Spring Boot which registers score points for different users and returns user position and high score list. Unit tests for "in memory" cache solution were also included.

### 1 - Starting your application:
1. You will need Maven</br>
2. Clone this repository or download the project [here](https://github.com/reirasdev/score-rank/archive/master.zip)
3. At the project root, run the following instruction:
   - *mvn spring-boot:run*</br>
   *Optionally, you can use the following command to load initial data for tests, 30.000 records are created</br>
      - *mvn spring-boot:run -Dspring-boot.run.arguments=--load.initialdata=true* 
4. Open your browser and go to:
   - http://localhost:8080/swagger-ui.html

### 2 - Running system with available data for tests
If you prefer, a instance of this system is already available at: </br>
http://ec2-18-228-59-179.sa-east-1.compute.amazonaws.com:8080/swagger-ui.html</br>
It has initial data loaded for tests.

### 3 - Run with Docker
Run this app with [docker-compose.yml](https://github.com/reirasdev/score-rank/blob/master/docker-compose.yml)</br>
Images available at https://hub.docker.com/r/reirasdev/score-rank
