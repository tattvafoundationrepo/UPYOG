FROM jelastic/maven:3.9.5-openjdk-21 AS build
#FROM ghcr.io/egovernments/alpine-maven-builder-jdk-8:1-master-na-6036091e AS build
ARG WORK_DIR
WORKDIR /app

# copy the project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven/start.sh ./start.sh

# not useful for stateless builds
# RUN mvn -B dependency:go-offline

COPY ${WORK_DIR}/src ./src
RUN mvn -B -f /app/pom.xml package


# Create runtime image
FROM jelastic/maven:3.9.5-openjdk-21
#FROM ghcr.io/egovernments/8-openjdk-alpine:latest


WORKDIR /opt/egov

COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/

RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
