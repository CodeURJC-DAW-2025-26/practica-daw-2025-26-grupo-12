#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: $0 <image-name>"
    exit 1
fi

IMAGE_NAME=$1

docker build -f docker/Dockerfile -t $IMAGE_NAME .