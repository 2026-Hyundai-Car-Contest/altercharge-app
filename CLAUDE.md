# AlterCharge (Pleos Connect 차량용 앱)

Pleos Connect SDK + Android(Kotlin) 기반 전기차 충전 일정 앱. 공모전 제출용이며 속도가 우선이다.

## 범위

- 이 저장소는 프론트엔드(차량용 Android 앱)만 다룬다. 백엔드는 별도 저장소이며 여기서는 수정하지 않는다. 서버 응답 규격은 백엔드 쪽과 맞춘다.

## Git / 커밋 규칙

- 규칙의 원본은 `CONTRIBUTING.md`다. `main` 하나에 짧은 작업 브랜치(`type/작업내용`), 커밋은 `type: 작업 내용`(한국어, scope 없음).
- 커밋 메시지와 PR 본문에 `Co-Authored-By: Claude`, "Generated with Claude Code" 등 AI 작성 표시를 넣지 않는다. PR 본문에는 실제 변경 내용만 쓴다.
- `main`에 직접 푸시하지 않는다. 모든 변경은 짧은 브랜치와 PR로 올려 Squash and Merge로 합친다. 공용 파일(Gradle, 매니페스트, 공통 컴포넌트)을 바꿀 때는 상대에게 먼저 알린다.
- `.githooks/pre-push`가 `main` 직접 푸시를 막는다. **AI는 `ALLOW_MAIN_PUSH=1`로 우회하지 않는다.** PR 병합(Squash)은 사용자가 허용한 경우에만 한다.
- 리뷰어 후보는 `.github/workflows/auto-assign.yml`의 `CANDIDATES`에 있고, 바꿀 때도 작업 브랜치와 PR로 올린다.
- 커밋 전에 `./gradlew lintDebug assembleDebug`가 통과하는지 확인한다.
- 키스토어, `local.properties`, 토큰, API 키, Pleos 클라이언트 ID/시크릿은 커밋하지 않는다.
- 푸시와 PR 생성은 사용자가 요청할 때만 한다.

## 개발 환경

- 스택: Kotlin, Jetpack Compose, Gradle 8.9, AGP 8.7.3, `minSdk 28` / `compileSdk·targetSdk 34`, Pleos Vehicle SDK(`gradle/libs.versions.toml`).
- **Gradle은 반드시 JDK 17로 돌린다.** Android Studio에 포함된 JDK 25로는 Gradle 8.9가 실패한다. `scripts/`가 JDK 17을 찾아 쓴다.
- 환경 세팅: `./scripts/setup.sh` (JDK 확인, API 34, Pleos 에뮬레이터 이미지, AVD). 실행: `./scripts/run.sh` (빌드, 에뮬레이터 켜기, 설치, 실행). 푸시 전 확인: `./scripts/check.sh`. 제출용 APK: `./scripts/release.sh <팀명>` (`keystore.properties` 필요, 키스토어와 비밀번호는 AI가 만들거나 저장소에 넣지 않는다).
- **라이선스 동의(Google SDK, 42dot Pleos SDK)는 사용자가 직접 한다.** AI가 `ACCEPT_LICENSES=yes`를 붙이거나 `yes`를 대신 입력하지 않는다. 사용자가 직접 읽고 동의했다고 말한 경우에만 그 옵션을 쓴다.
- 차량 에뮬레이터는 화면이 여러 개다. 앱은 메인 화면(display 0)에 띄운다: `adb shell am start -W --display 0 -n com.altercharge.app/.MainActivity`
- 라이브러리 버전을 올릴 때 `compileSdk 34`에서 빌드되는지 확인한다. (`core-ktx 1.15`는 compileSdk 35가 필요해서 1.13.1을 쓴다.)
- Pleos Vehicle API 퍼미션은 Pleos App Market과 협의·승인된 것만 매니페스트에 선언한다.
