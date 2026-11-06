#!/usr/bin/env bash
# Rebuild all services and (re)start them in Docker.
#
# Usage:
#   ./run-docker.sh        # build + start, show logs in this terminal (Ctrl+C to stop)
#   ./run-docker.sh -d     # build + start in background

set -e                                   # stop the script if any step fails
cd "$(dirname "$0")"                     # always run from the project root

# Maven needs Java 21 (system JAVA_HOME points to Java 8)
export JAVA_HOME=/usr/lib/jvm/jdk-21.0.7-oracle-x64

# Make sure Docker is running
if ! docker info > /dev/null 2>&1; then
  echo "Docker is not running. Start Docker Desktop and try again."
  exit 1
fi

echo "==> 1/3 Stopping old containers"
docker compose down

echo "==> 2/3 Building jars"
./mvnw clean package -DskipTests

echo "==> 3/3 Building images and starting containers"
if [ "$1" == "-d" ]; then
  docker compose up --build -d
  echo
  echo "Started in background."
  echo "  Eureka:  http://localhost:8761"
  echo "  Gateway: http://localhost:8084/api/books"
  echo "  Logs:    docker compose logs -f"
  echo "  Stop:    docker compose down"
else
  docker compose up --build
fi
