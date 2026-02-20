#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYSTORE_PATH="${SCRIPT_DIR}/keystore.p12"
ALIAS="scissors-please"
STOREPASS="potato"
VALIDITY_DAYS=3650

if [[ -f "${KEYSTORE_PATH}" ]]; then
  echo "El keystore ya existe en: ${KEYSTORE_PATH}"
  echo "Si quieres regenerarlo, elimina el archivo y ejecuta de nuevo el script."
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

echo "Keystore generado en: ${KEYSTORE_PATH}"