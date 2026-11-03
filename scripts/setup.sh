#!/usr/bin/env bash
# AlterCharge 개발 환경 세팅 (macOS / Linux). 여러 번 실행해도 이미 된 단계는 건너뛴다.
#
#   ./scripts/setup.sh
#
# 하는 일: git 훅 켜기 → JDK 17·SDK 확인 → Android API 34 설치 → Pleos 에뮬레이터 이미지 설치 → 가상 기기(AVD) 생성
# 라이선스(Google Android SDK, 42dot Pleos SDK)는 직접 읽고 'yes'를 입력해야 설치한다.
# 이미 읽고 동의한 경우에만 ACCEPT_LICENSES=yes ./scripts/setup.sh 로 질문을 건너뛸 수 있다.
set -euo pipefail
cd "$(dirname "$0")/.."
# shellcheck source=scripts/lib.sh
. scripts/lib.sh

ask_license() { # $1: 라이선스 이름, $2: 확인 위치
  [ "${ACCEPT_LICENSES:-}" = "yes" ] && return 0
  [ -t 0 ] || die "$1 동의가 필요해요. 터미널에서 직접 실행하거나, 이미 읽고 동의했다면 ACCEPT_LICENSES=yes 로 실행해 주세요."
  echo "  $1 을(를) 읽어 보세요: $2"
  read -r -p "  동의하면 'yes' 를 입력: " answer
  [ "$answer" = "yes" ] || die "동의하지 않아 중단합니다."
}

say "1/6 git 훅 켜기 (커밋 메시지 형식 검사)"
git config core.hooksPath .githooks
ok "core.hooksPath = .githooks"

say "2/6 JDK 17과 Android SDK 확인"
prepare_env
SDKMANAGER=$(cmdline_tool "$SDK" sdkmanager) || die "Android SDK Command-line Tools가 없어요.
  Android Studio > Settings > Languages & Frameworks > Android SDK > SDK Tools 에서
  'Android SDK Command-line Tools (latest)'를 설치한 뒤 다시 실행해 주세요."
AVDMANAGER=$(cmdline_tool "$SDK" avdmanager) || die "avdmanager를 찾지 못했어요."

say "3/6 Android API 34 플랫폼"
if [ -d "$SDK/platforms/android-34" ]; then
  ok "이미 설치됨"
else
  ask_license "Android SDK 라이선스" "https://developer.android.com/studio/terms"
  yes | "$SDKMANAGER" "platforms;android-34" >/dev/null
  ok "설치 완료"
fi

say "4/6 Pleos Connect 에뮬레이터 이미지 (약 4.7GB 내려받기, 압축 해제 후 약 20GB)"
case "$(uname -m)" in
  arm64|aarch64) XML_URL="https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml" ;;
  *)             XML_URL="https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml" ;;
esac
CACHE="$HOME/.cache/altercharge"; mkdir -p "$CACHE"
XML="$CACHE/pleos-sys-img.xml"
curl -fsSL -m 60 -o "$XML" "$XML_URL" || die "Pleos 이미지 목록을 받지 못했어요. 인터넷 연결을 확인해 주세요."
ABI=$(sed -n 's:.*<abi>\(.*\)</abi>.*:\1:p' "$XML" | head -1)
ZIP_URL=$(sed -n 's:.*<url>\(.*\)</url>.*:\1:p' "$XML" | head -1)
SHA1=$(sed -n 's:.*<checksum>\(.*\)</checksum>.*:\1:p' "$XML" | head -1)
[ -n "$ABI" ] && [ -n "$ZIP_URL" ] && [ -n "$SHA1" ] || die "이미지 목록 형식이 달라졌어요. README의 수동 설치 방법을 따라 주세요."
IMG_DIR="$SDK/system-images/android-34/android-automotive/$ABI"

if [ -f "$IMG_DIR/system.img" ]; then
  ok "이미 설치됨: $IMG_DIR"
else
  avail_kb=$(df -k "$SDK" | awk 'NR==2 {print $4}')
  [ "${avail_kb:-0}" -gt 26000000 ] || die "디스크 여유 공간이 부족해요. 약 26GB 이상 필요합니다."
  ask_license "42dot Pleos Connect SDK 라이선스(Software Development Kit License Agreement)" "$XML_URL (파일 안의 license 항목)"
  ZIP="$CACHE/$(basename "$ZIP_URL")"
  echo "  내려받는 중... (끊기면 같은 명령을 다시 실행하면 이어받아요)"
  curl -fL -C - --progress-bar -o "$ZIP" "$ZIP_URL"
  echo "  체크섬 확인 중..."
  actual=$(shasum -a 1 "$ZIP" | awk '{print $1}')
  [ "$actual" = "$SHA1" ] || die "체크섬이 맞지 않아요. $ZIP 를 지우고 다시 실행해 주세요."
  ok "체크섬 일치"
  TOP=$(unzip -Z1 "$ZIP" | head -1 | cut -d/ -f1)
  PARENT="$SDK/system-images/android-34/android-automotive"
  mkdir -p "$PARENT"
  echo "  압축 푸는 중... (몇 분 걸려요)"
  unzip -q -o "$ZIP" -d "$PARENT"
  rm -rf "$IMG_DIR"; mv "$PARENT/$TOP" "$IMG_DIR"
  rm -f "$ZIP"
  ok "설치 완료: $IMG_DIR"
fi

say "5/6 가상 기기(AVD) 만들기: $AVD_NAME"
if [ -f "$HOME/.android/avd/$AVD_NAME.ini" ]; then
  ok "이미 있음"
else
  DEVICE=$(sed -n 's:.*<d\:id>\(.*\)</d\:id>.*:\1:p' "$IMG_DIR/devices.xml" | head -1)
  DEVICE=${DEVICE:-Pleos_Connect_v2_0}
  echo no | "$AVDMANAGER" create avd -n "$AVD_NAME" -k "system-images;android-34;android-automotive;$ABI" -d "$DEVICE" --force >/dev/null
  CFG="$HOME/.android/avd/$AVD_NAME.avd/config.ini"
  # Pleos 문서 권장: RAM 4GB, GPU 호스트, Default boot Cold
  sed -e 's/^hw.ramSize=.*/hw.ramSize=4096/' \
      -e 's/^hw.gpu.enabled=.*/hw.gpu.enabled=yes/' \
      -e 's/^hw.gpu.mode=.*/hw.gpu.mode=host/' "$CFG" > "$CFG.tmp" && mv "$CFG.tmp" "$CFG"
  grep -q '^fastboot.forceColdBoot' "$CFG" || echo 'fastboot.forceColdBoot=yes' >> "$CFG"
  ok "생성 완료 (RAM 4GB, Cold boot)"
fi

say "6/6 끝"
ok "환경 세팅이 끝났어요. 앱을 실행하려면: ./scripts/run.sh"
