#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: $0 <image-name>"
    exit 1
fi

IMAGE_NAME=$1

WEB_IMAGE=sa4dus/scissors-please envsubst < docker/docker_compose.yml > docker/docker-compose.resolved.yml
docker compose -f docker/docker-compose.resolved.yml publish $IMAGE_NAME --with-env