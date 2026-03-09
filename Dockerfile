FROM maven:3.9-eclipse-temurin-17

# Install Google Chrome
RUN apt-get update \
    && apt-get install -y wget gnupg2 curl \
    && curl -fsSL https://dl.google.com/linux/linux_signing_key.pub \
         | gpg --dearmor -o /usr/share/keyrings/googlechrome.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/googlechrome.gpg] \
         http://dl.google.com/linux/chrome/deb/ stable main" \
         > /etc/apt/sources.list.d/chrome.list \
    && apt-get update && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Cache Maven dependencies before copying source
COPY pom.xml .
RUN mvn dependency:resolve -q

# Copy source and compile (tests compiled too so the image is ready to run)
COPY . .
RUN mvn compile test-compile -q -DskipTests

# UTF-8 console output (needed for Vietnamese log messages)
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Default: run smoke tests
CMD ["mvn", "test", "-Dheadless=true"]
