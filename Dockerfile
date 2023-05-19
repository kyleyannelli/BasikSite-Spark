FROM maven:3.8.3-openjdk-17

WORKDIR /app
COPY . /app

# Can't use square brackets here as && is a shell operation
CMD /bin/bash -c "mvn liquibase:update && mvn compile exec:java"
