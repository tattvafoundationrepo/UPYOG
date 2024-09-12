# Step 1: Build Stage
FROM upyogio/alpine-maven-builder-jdk-8:1-master-NA-6036091e AS build
ARG WORK_DIR
WORKDIR /app

# Copy the project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven/start.sh ./start.sh

# Copy the source code and build the project
COPY ${WORK_DIR}/src ./src
RUN mvn -B -f /app/pom.xml package

# Step 2: Runtime Stage
FROM upyogio/8-openjdk-alpine

WORKDIR /opt/egov

# Copy the built application and the startup script from the build stage
COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/

# Copy the certificate to the runtime image
COPY ${WORK_DIR}/smsgwsmsgovin.cer /opt/egov/smsgwsmsgovin.cer

# Add the certificate to the JVM truststore
RUN keytool -importcert -noprompt -trustcacerts \
    -alias msdgweb_mgov_gov_in \
    -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit \
    -file /opt/egov/smsgwsmsgovin.cer

# Ensure the start script is executable
RUN chmod +x /opt/egov/start.sh

# Command to start the application
CMD ["/opt/egov/start.sh"]
