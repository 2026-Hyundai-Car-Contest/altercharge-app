#!/usr/bin/env bash
# 푸시 전 확인: lint와 디버그 빌드 (CI가 돌리는 것과 같다)
#
#   ./scripts/check.sh
set -euo pipefail
cd "$(dirname "$0")/.."
# shellcheck source=scripts/lib.sh
. scripts/lib.sh

say "환경 확인"
prepare_env

say "lint와 빌드"
./gradlew lintDebug assembleDebug -q
ok "통과했어요. 푸시해도 돼요."
