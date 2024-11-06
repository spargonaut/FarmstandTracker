FROM amazoncorretto:21-al2-jdk AS build

COPY . /home/gradle
WORKDIR /home/gradle
RUN ./gradlew --no-daemon clean build

FROM amazoncorretto:21-al2-jdk
EXPOSE 8080
COPY --from=build /home/gradle/build/libs/FarmstandTracker-all.jar /app/

ENTRYPOINT ["java","-jar","/app/FarmstandTracker-all.jar"]

