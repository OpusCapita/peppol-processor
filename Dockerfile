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

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/peppol-processor.jar .

RUN apt-get update && apt-get -y install redis-server

HEALTHCHECK --interval=15s --timeout=30s --start-period=40s --retries=15 \
  CMD wget --quiet --tries=1 --spider http://localhost:3038/api/health/check || exit 1

EXPOSE 3038
CMD start.sh