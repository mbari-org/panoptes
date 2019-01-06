#!/usr/bin/env bash

echo "--- Building panoptes (reminder: run docker login first!!)"
sbt pack && \
    docker build -t mbari/panoptes . && \
    docker push mbari/panoptes
