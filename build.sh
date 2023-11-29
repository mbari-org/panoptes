#!/usr/bin/env bash

echo "--- Building panoptes (reminder: run docker login first!!)"

BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"`
VCS_REF=`git tag | sort -V | tail -1`

SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
echo "Working directory is $SCRIPT_DIR"
cd $SCRIPT_DIR

sbt pack

ARCH=$(uname -m)
if [[ $ARCH == 'arm64' ]]; then
    # https://betterprogramming.pub/how-to-actually-deploy-docker-images-built-on-a-m1-macs-with-apple-silicon-a35e39318e97
    docker buildx build \
      --platform linux/amd64,linux/arm64 \
      -t mbari/panoptes:${VCS_REF} \
      -t mbari/panoptes:latest \
      --push . && \
    docker pull mbari/panoptes
else
    docker build --build-arg BUILD_DATE=$BUILD_DATE \
                 --build-arg VCS_REF=$VCS_REF \
                  -t mbari/panoptes:${VCS_REF} \
                  -t mbari/panoptes:latest . && \
    docker push mbari/panoptes
fi
 
