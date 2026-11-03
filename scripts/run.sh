#!/usr/bin/env bash
# 앱을 빌드하고 Pleos 에뮬레이터에 설치·실행한다. (에뮬레이터가 꺼져 있으면 켠다)
#
#   ./scripts/run.sh
set -euo pipefail
cd "$(dirname "$0")/.."
# shellcheck source=scripts/lib.sh
. scripts/lib.sh

say "환경 확인"
prepare_env

say "빌드 (assembleDebug)"
./gradlew assembleDebug -q
APK=$(ls app/build/outputs/apk/debug/*.apk | head -1)
ok "$APK"

say "에뮬레이터"
if ! adb devices | grep -q '^emulator-.*device$'; then
  [ -f "$HOME/.android/avd/$AVD_NAME.ini" ] || die "가상 기기($AVD_NAME)가 없어요. 먼저 ./scripts/setup.sh 를 실행해 주세요."
  echo "  켜는 중..."
  nohup emulator -avd "$AVD_NAME" -no-snapshot-load >"${TMPDIR:-/tmp}/altercharge-emulator.log" 2>&1 &
  adb wait-for-device
  for _ in $(seq 1 60); do
    [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && break
    sleep 3
  done
fi
ok "에뮬레이터 준비됨"

say "설치와 실행"
adb install -r "$APK" >/dev/null
# 차량 에뮬레이터는 화면이 여러 개라서 메인 화면(0)에 띄운다.
adb shell am start -W --display 0 -n "$APP_ID/.MainActivity" >/dev/null
ok "실행했어요. 에뮬레이터 화면을 확인해 주세요."
