## using multistage docker build for speed
## temp container to build
FROM openjdk:8 AS TEMP_BUILD_IMAGE

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

COPY build.gradle settings.gradle gradlew $APP_HOME
COPY gradle $APP_HOME/gradle
COPY . $APP_HOME

RUN chmod +x ./gradlew
RUN ./gradlew build || return 0

## actual container
FROM openjdk:8
LABEL author="Ibrahim Bilge <Ibrahim.Bilge@opuscapita.com>"

# https://github.com/prometheus/jmx_exporter
ADD https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.14.0/jmx_prometheus_javaagent-0.14.0.jar /opt/jmx_prometheus_javaagent.jar
COPY jmx_exporter_config.yml /opt/
RUN chmod 777 /opt/jmx_prometheus_javaagent.jar

## setting heap size automatically to the container memory limits
ENV JAVA_OPTS="\
 -XX:+UseContainerSupport\
 -XX:MaxRAMPercentage=75.0\
 -XshowSettings:vm\
 -javaagent:/opt/jmx_prometheus_javaagent.jar=3067:/opt/jmx_exporter_config.yml"

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/peppol-processor.jar .

HEALTHCHECK --interval=15s --timeout=30s --start-period=40s --retries=15 \
  CMD wget --quiet --tries=1 --spider http://localhost:3038/api/health/check || exit 1

EXPOSE 3038
EXPOSE 3067
ENTRYPOINT exec java $JAVA_OPTS -jar peppol-processor.jar