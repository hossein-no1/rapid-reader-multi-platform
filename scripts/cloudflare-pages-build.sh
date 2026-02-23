#!/usr/bin/env bash
set -euo pipefail

# Cloudflare Pages build environment doesn't ship with a JDK.
# This script bootstraps a local Temurin JDK and then runs Gradle.

JDK_VERSION="${JDK_VERSION:-17}"
JDK_DIR="${JDK_DIR:-.cf-jdk}"

if [[ ! -x "${JDK_DIR}/bin/java" ]]; then
  echo "Bootstrapping Temurin JDK ${JDK_VERSION} into ${JDK_DIR}..."
  rm -rf "${JDK_DIR}"
  mkdir -p "${JDK_DIR}"

  # Linux x64 (Cloudflare Pages build environment)
  JDK_URL="https://api.adoptium.net/v3/binary/latest/${JDK_VERSION}/ga/linux/x64/jdk/hotspot/normal/eclipse"
  curl -fsSL "${JDK_URL}" -o /tmp/temurin-jdk.tar.gz
  tar -xzf /tmp/temurin-jdk.tar.gz -C "${JDK_DIR}" --strip-components=1
fi

export JAVA_HOME="$(cd "${JDK_DIR}" && pwd)"
export PATH="${JAVA_HOME}/bin:${PATH}"

# Override any machine-specific org.gradle.java.home set in gradle.properties.
./gradlew -Dorg.gradle.java.home="${JAVA_HOME}" ${GRADLE_ARGS:-} :composeApp:jsBrowserProductionWebpack

