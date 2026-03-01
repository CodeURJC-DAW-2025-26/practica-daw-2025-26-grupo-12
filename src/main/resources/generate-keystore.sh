#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
KEYSTORE_PATH="${SCRIPT_DIR}/keystore.p12"
ALIAS="scissors-please"

if [[ -f "${REPO_ROOT}/.env" ]]; then
  set -a
  source "${REPO_ROOT}/.env"
  set +a
fi

STOREPASS="${SERVER_SSL_KEY_STORE_PASSWORD:-password}"
VALIDITY_DAYS=3650

if [[ -f "${KEYSTORE_PATH}" ]]; then
  echo "The keystore already exists at: ${KEYSTORE_PATH}"
  echo "If you want to regenerate it, delete the file and run the script again."
  exit 0
fi

keytool -genkeypair \
  -alias "${ALIAS}" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "${VALIDITY_DAYS}" \
  -storetype PKCS12 \
  -keystore "${KEYSTORE_PATH}" \
  -storepass "${STOREPASS}" \
  -keypass "${STOREPASS}" \
  -dname "CN=localhost, OU=Dev, O=ScissorsPlease, L=Local, ST=Local, C=ES" \
  -ext "SAN=DNS:localhost,IP:127.0.0.1" \
  -noprompt

echo "Keystore generated at: ${KEYSTORE_PATH}"
