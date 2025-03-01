FROM eclipse-temurin:21

ARG BUILD_DATE
ARG VCS_REF
ARG VERSION
LABEL org.label-schema.build-date=$BUILD_DATE \
  org.label-schema.name="panoptes" \
  org.label-schema.description="A RESTful microservice for storing and managing images" \
  org.label-schema.url="https://mbari-media-management.github.io/" \
  org.label-schema.vcs-ref=$VCS_REF \
  org.label-schema.vcs-url="https://github.com/mbari-media-management/panoptes" \
  org.label-schema.vendor="Monterey Bay Aquarium Research Institute" \
  org.label-schema.schema-version="1.0" \
  maintainer="Brian Schlining <brian@mbari.org>"

ENV APP_HOME=/opt/panoptes

RUN mkdir -p ${APP_HOME}

COPY target/universal/stage ${APP_HOME}

EXPOSE 8080

ENTRYPOINT $APP_HOME/bin/panoptes