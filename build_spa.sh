#!/bin/bash
set -e

PROJECT_ROOT=$(pwd)
FRONTEND_DIR="$PROJECT_ROOT/frontend"
BACKEND_STATIC_DIR="$PROJECT_ROOT/backend/src/main/resources/static/new"

echo "Starting SPA build process..."

cd "$FRONTEND_DIR"
echo "Installing frontend dependencies..."
npm install

echo "Building frontend project..."
npm run build

echo "Preparing static assets directory..."
mkdir -p "$BACKEND_STATIC_DIR"
rm -rf "$BACKEND_STATIC_DIR"/*

echo "Deploying build artifacts to backend..."
cp -r build/client/* "$BACKEND_STATIC_DIR/"

echo "Build and deployment completed successfully."
