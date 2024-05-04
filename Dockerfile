FROM openjdk:18
COPY target/PracticalTestAssignment-0.0.1-SNAPSHOT.jar .
CMD ["java","-jar","PracticalTestAssignment-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080