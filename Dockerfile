FROM navikt/java:14
COPY init.sh /init-scripts/init.sh
COPY build/libs/app.jar /app/

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote \
               -Dhttps.proxyHost=webproxy-nais.nav.no \
               -Dhttps.proxyPort=8088 \
               -Dhttp.nonProxyHosts=*.adeo.no|*.preprod.local|*.intern.nav.no"

