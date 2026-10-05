Make sure Docker Desktop is running, then open a terminal in the starter_kit directory and run docker compose up -d to start PostgreSQL, Redis, and Kafka. 
Create the required Kafka topics using docker exec bank-kafka sh /scripts/create-topics.sh.
Next, start the Spring Boot application from the horizon-api Folder using mvn spring-boot:run or directly through an IDE. Once the application starts successfully, the API will be available at http://localhost:8080.

