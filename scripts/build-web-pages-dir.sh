#!/usr/bin/env bash
set -euo pipefail

# Builds the production JS bundle and assembles a Cloudflare Pages-ready folder
# containing index.html + static resources + the generated JS/WASM artifacts.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

./gradlew :composeApp:jsBrowserProductionWebpack

OUT_DIR="composeApp/build/pages"
RES_DIR="composeApp/src/webMain/resources"
ARTIFACT_DIR="composeApp/build/kotlin-webpack/js/productionExecutable"

rm -rf "${OUT_DIR}"
mkdir -p "${OUT_DIR}"

cp "${RES_DIR}/index.html" "${OUT_DIR}/"
cp "${RES_DIR}/styles.css" "${OUT_DIR}/"
cp "${RES_DIR}/rapid_reader_logo.png" "${OUT_DIR}/"

# Copy the generated bundle(s) (composeApp.js, *.wasm, sourcemaps, etc.)
cp -R "${ARTIFACT_DIR}/." "${OUT_DIR}/"

echo "Pages output ready at: ${OUT_DIR}"

