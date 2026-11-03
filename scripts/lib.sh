#!/usr/bin/env bash
# setup.sh / run.sh 공용 함수. 직접 실행하지 않는다.

AVD_NAME="Pleos_Connect_API34"
APP_ID="com.altercharge.app"

say() { printf '\n\033[1m▶ %s\033[0m\n' "$*"; }
ok()  { printf '  ✓ %s\n' "$*"; }
warn(){ printf '  ! %s\n' "$*"; }
die() { printf '\n✗ %s\n' "$*" >&2; exit 1; }

# JDK 17을 찾는다. (Android Studio에 포함된 JDK 25는 Gradle 8.9와 맞지 않는다)
find_jdk17() {
  if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ] \
     && "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"17\.'; then
    echo "$JAVA_HOME"; return 0
  fi
  if [ -x /usr/libexec/java_home ]; then
    h=$(/usr/libexec/java_home -v 17 2>/dev/null || true)
    [ -n "$h" ] && { echo "$h"; return 0; }
  fi
  for h in \
    /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
    /usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
    /usr/lib/jvm/java-17-openjdk* /usr/lib/jvm/temurin-17*; do
    [ -x "$h/bin/java" ] && { echo "$h"; return 0; }
  done
  return 1
}

# Android SDK 위치를 찾는다.
find_sdk() {
  for d in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" "$HOME/Library/Android/sdk" "$HOME/Android/Sdk"; do
    [ -n "$d" ] && [ -d "$d" ] && { echo "$d"; return 0; }
  done
  return 1
}

# cmdline-tools 실행 파일 경로 (latest 또는 첫 번째 버전 폴더)
cmdline_tool() {
  local sdk="$1" name="$2"
  for d in "$sdk/cmdline-tools/latest" "$sdk"/cmdline-tools/*; do
    [ -x "$d/bin/$name" ] && { echo "$d/bin/$name"; return 0; }
  done
  return 1
}

# 환경(JAVA_HOME, ANDROID_HOME, SDK)을 준비한다.
prepare_env() {
  JDK=$(find_jdk17) || die "JDK 17을 찾지 못했어요.
  macOS:  brew install openjdk@17
  설치 후 이 스크립트를 다시 실행해 주세요."
  export JAVA_HOME="$JDK"
  SDK=$(find_sdk) || die "Android SDK를 찾지 못했어요. Android Studio를 설치하고 한 번 실행해 주세요."
  export ANDROID_HOME="$SDK"
  export PATH="$JAVA_HOME/bin:$SDK/platform-tools:$SDK/emulator:$PATH"
  ok "JDK 17: $JAVA_HOME"
  ok "Android SDK: $SDK"
}
