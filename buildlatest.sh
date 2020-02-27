echo "Bygger syfogsak latest for docker compose utvikling"

mvn clean install -D skipTests

docker build . -t syfogsak:latest
