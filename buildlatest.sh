#!/bin/bash
echo "Bygger sykepengesoknad-arkivering-oppgave latest for docker compose utvikling"
rm -rf ./build
./gradlew bootJar
docker build -t sykepengesoknad-arkivering-oppgave:latest .
