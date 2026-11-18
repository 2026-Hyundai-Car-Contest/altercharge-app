#!/usr/bin/env bash
# 푸시 전 확인: lint, 디버그 빌드, release 컴파일, 단위 테스트 (CI가 돌리는 것과 같다)
#
#   ./scripts/check.sh
set -euo pipefail
cd "$(dirname "$0")/.."
# shellcheck source=scripts/lib.sh
. scripts/lib.sh

say "환경 확인"
prepare_env

say "lint, 빌드, release 컴파일, 단위 테스트"
./gradlew lintDebug assembleDebug compileReleaseKotlin testDebugUnitTest -q
ok "통과했어요. 푸시해도 돼요."
