#!/usr/bin/env bash
# 공모전 제출용 release APK를 만든다. 서명 키스토어가 있는 사람만 실행할 수 있다.
#
#   ./scripts/release.sh <팀명> [앱이름]
#   예) ./scripts/release.sh TeamA          → apk/TeamA_AlterCharge_release.apk
#
# 준비: 저장소 루트에 keystore.properties가 있어야 한다. (저장소에 올라가지 않는다. 보관 담당에게 받는다)
#   storeFile=/절대경로/my.jks
#   storePassword=...
#   keyAlias=...
#   keyPassword=...
set -euo pipefail
cd "$(dirname "$0")/.."
# shellcheck source=scripts/lib.sh
. scripts/lib.sh

TEAM="${1:-}"
NAME="${2:-AlterCharge}"
[ -n "$TEAM" ] || die "사용법: ./scripts/release.sh <팀명> [앱이름]"
case "$TEAM$NAME" in *[[:space:]]*) die "팀명과 앱이름에는 공백을 쓸 수 없어요." ;; esac
[ -f keystore.properties ] || die "keystore.properties가 없어요.
  키스토어는 저장소에 올리지 않아요. 키스토어 보관 담당에게 받아서 저장소 루트에 두세요. (README 참고)"

say "환경 확인"
prepare_env

say "서버 주소 확인"
# gradle.properties의 altercharge.releaseBaseUrl (출시 빌드의 BuildConfig.BASE_URL)
RELEASE_BASE_URL="$(grep -E '^altercharge\.releaseBaseUrl=' gradle.properties | cut -d= -f2- || true)"
if [ -z "$RELEASE_BASE_URL" ]; then
  warn "출시용 서버 주소(altercharge.releaseBaseUrl)가 비어 있어요. 서버를 쓰는 화면은 동작하지 않아요."
else
  ok "출시 서버 주소: $RELEASE_BASE_URL"
fi

say "release 빌드"
./gradlew assembleRelease -q
SRC="app/build/outputs/apk/release/app-release.apk"
[ -f "$SRC" ] || die "서명된 APK(app-release.apk)가 없어요. keystore.properties의 값을 확인해 주세요."

say "서명 확인"
SIGNER=""
for jar in "$SDK"/build-tools/*/lib/apksigner.jar; do [ -f "$jar" ] && SIGNER="$jar"; done
if [ -n "$SIGNER" ]; then
  "$JAVA_HOME/bin/java" -jar "$SIGNER" verify "$SRC" >/dev/null || die "서명 검증에 실패했어요. keystore.properties의 값을 확인해 주세요."
  ok "서명 검증 통과"
else
  warn "apksigner를 찾지 못해 서명 검증은 건너뛰었어요."
fi

mkdir -p apk
DEST="apk/${TEAM}_${NAME}_release.apk"
cp "$SRC" "$DEST"
ok "만들었어요: $DEST ($(du -h "$DEST" | awk '{print $1}'))"
echo "  이 파일은 직접 git add 해서 올려 주세요. (apk/ 안의 APK만 올라가요)"
