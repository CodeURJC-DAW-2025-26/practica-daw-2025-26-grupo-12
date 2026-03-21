#!/bin/bash

if [ -z "$2" ]; then
    echo "Usage: $0 <image-name> <web-image-name>"
    exit 1
fi

IMAGE_NAME=$1
WEB_IMAGE_VALUE=$2

echo "IMAGE_NAME: $IMAGE_NAME"
echo "WEB_IMAGE: $WEB_IMAGE_VALUE"

export IMAGE_NAME=$IMAGE_NAME
export WEB_IMAGE=$WEB_IMAGE_VALUE

envsubst < docker/docker_compose.yml > docker/docker-compose.resolved.yml
docker compose -f docker/docker-compose.resolved.yml publish $IMAGE_NAME --with-env