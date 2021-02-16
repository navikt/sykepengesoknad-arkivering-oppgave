#!/bin/bash
echo "Bygger syfogsak latest for docker compose utvikling"
rm -rf ./build
./gradlew bootJar
docker build -t syfogsak:latest .
