FROM maven:3.8.3-openjdk-17

WORKDIR /app
COPY . /app

CMD ["mvn", "compile", "exec:java"]
