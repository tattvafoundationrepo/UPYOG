# Use the custom Maven image as the base for building the application
FROM tattvafoundation/maven3.9:latest AS build
ARG WORK_DIR
WORKDIR /app

# Copy the project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven/start.sh ./start.sh

# Not useful for stateless builds
# RUN mvn -B dependency:go-offline

COPY ${WORK_DIR}/src ./src
RUN mvn -B -f /app/pom.xml package

# Create runtime image using the same custom Maven image
FROM tattvafoundation/maven3.9:latest

WORKDIR /opt/egov

COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/

RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
