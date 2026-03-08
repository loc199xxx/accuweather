FROM maven:3.9-eclipse-temurin-17 AS builder

# Chrome for Selenium
RUN apt-get update && apt-get install -y wget gnupg2 unzip \
    && wget -qO- https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/chrome.list \
    && apt-get update && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve -q

COPY . .
RUN mvn compile -q -DskipTests

ENV HEADLESS=true
