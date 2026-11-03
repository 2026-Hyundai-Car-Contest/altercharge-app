# AlterCharge (얼터차지)

출퇴근길 차 안에서, 집이나 회사에 도착하기 전에 "오늘 충전해야 하는지"와 "자리가 없으면 어디로 갈지"를 음성으로 정해 주는 **전기차 충전 일정 앱**.
Pleos Connect(Android Automotive 기반) 차량용 앱이며, 현대차그룹 차량용 앱 개발 공모전 출품작이다.

- 스택: Android (Kotlin) + Pleos Connect SDK
- 사용 SDK: Vehicle(배터리·주행거리), NaviHelper(내비 목적지), TextToSpeech / SpeechToText(음성), FusedLocation(거점 진입)
- 이 저장소는 **프론트엔드(차량용 Android 앱)만** 다룬다. 백엔드는 별도 저장소에서 개발하며, 여기서는 API를 호출하는 쪽만 만든다.
- 기획: Notion "얼터차지 for Pleos Connect 1-pager" 참고

## 용어 (처음이면 먼저 읽기)

| 용어 | 뜻 |
|---|---|
| Pleos | 현대차그룹의 차량 소프트웨어 브랜드. 차량 화면(인포테인먼트)에서 돌아가는 앱을 만들 수 있게 SDK를 제공한다. |
| Pleos Playground | Pleos 개발자 포털. 문서, SDK, 프로젝트 신청이 여기에 있다. |
| Pleos Connect | 차량 화면 시스템. Android Automotive OS 기반이라 일반 Android 앱 개발 방식으로 만든다. |
| 에뮬레이터 | 컴퓨터에서 차량 화면을 흉내 내는 가상 기기. 실차 없이 앱을 실행해 본다. |
| SDK | 차량 데이터(배터리 등)를 읽는 라이브러리. Gradle 의존성으로 추가한다. |

## 1. 환경 세팅

**스크립트 한 번으로 끝난다.** (macOS / Linux. Windows는 아래 "수동 설치"를 따른다.)
이미지가 크므로 **와이파이에서, 디스크 여유 26GB 이상**으로 시작한다. 처음에는 30분~1시간 걸린다.

### 1-1. 먼저 준비할 것 (직접 설치)

| 필요한 것 | 확인 / 설치 |
|---|---|
| [Android Studio](https://developer.android.com/studio) 최신 버전 | 설치 후 **한 번 실행**해서 SDK를 받게 한다. Settings > Android SDK > SDK Tools 에서 **Android SDK Command-line Tools (latest)** 를 체크해 설치한다. |
| **JDK 17** | `brew install openjdk@17` (Mac). Android Studio에 들어 있는 JDK 25는 Gradle 8.9와 **맞지 않아서** 쓰지 않는다. 스크립트가 JDK 17을 알아서 찾는다. |
| Git | `git --version` |
| 여유 RAM 8GB 이상 | 에뮬레이터가 4GB를 쓴다. |

### 1-2. 저장소 받고 스크립트 실행

```bash
git clone https://github.com/2026-Hyundai-Car-Contest/altercharge-app.git
cd altercharge-app
./scripts/setup.sh
```

`setup.sh`가 하는 일 (이미 된 단계는 건너뛰므로 여러 번 실행해도 된다):

1. git 훅 켜기 (커밋 메시지 형식 검사)
2. JDK 17과 Android SDK 확인
3. Android API 34 플랫폼 설치
4. Pleos Connect 에뮬레이터 이미지 설치 (4.7GB 내려받기 → 체크섬 검증 → 압축 해제)
5. 가상 기기 `Pleos_Connect_API34` 생성 (RAM 4GB, Cold boot)

**라이선스는 직접 읽고 동의해야 한다.** 설치가 필요한 단계에서 Google Android SDK 라이선스와 42dot Pleos SDK 라이선스 동의 질문이 나오고, `yes`를 직접 입력해야 진행된다. 이미 읽고 동의했다면 `ACCEPT_LICENSES=yes ./scripts/setup.sh`로 질문을 건너뛸 수 있다.

> **AI(Claude 등)에게 시킬 때**: "scripts/setup.sh를 실행해서 환경 세팅해 줘"라고 하면 된다. 단, 라이선스 동의는 AI가 대신하지 못하게 본인이 직접 입력한다.

### 1-3. 앱 실행

```bash
./scripts/run.sh
```

빌드하고, 에뮬레이터가 꺼져 있으면 켜고, 앱을 설치해서 실행한다. 차량 화면 오른쪽 메인 영역에 **AlterCharge**가 보이면 성공이다. 계기판 쪽 화면(0km/h, P R N D)은 정상적인 차량 에뮬레이터 화면이다.

Android Studio에서 실행하려면 `altercharge-app` 폴더를 Open 하고, 실행 기기로 `Pleos_Connect_API34`를 고른 뒤 ▶ Run 한다. 이때도 **Gradle JDK를 17로 지정**해야 한다. (Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK)

### 1-4. Pleos Playground 가입 (직접)

1. [Pleos Playground](https://developers.pleos.ai/playground)에서 가입하고 로그인한다. (오른쪽 위 "로그인")
2. **프로젝트(`AlterCharge`)는 이미 생성돼 있다.** 새로 만들지 않고, 가입한 이메일을 프로젝트 소유자에게 알려 팀 멤버로 초대받는다. (Playground > Workspace > My Project > 팀 관리)
3. 아래 SDK는 신청 없이 바로 쓸 수 있다: **Vehicle State, NaviHelper**
4. 아래 SDK는 2026-11-03 기준 **Coming Soon(신청 불가)** 이다: FusedLocation, TextToSpeech, SpeechToText, Vehicle Control. 음성과 거점 진입 감지는 제공되기 전까지 화면과 버튼 대체 방식으로 만든다.
5. 클라이언트 ID/시크릿은 서버 연동 SDK(TTS, STT)에서 필요한 값이다. **코드와 저장소에 절대 넣지 않는다.** 필요하면 `secrets.properties`에 두면 `.gitignore`로 제외된다.

### 프로젝트 구성 (이미 만들어져 있음)

- Kotlin + Jetpack Compose, `minSdk 28`(Pleos 요구사항), `compileSdk/targetSdk 34`, Gradle 8.9, AGP 8.7.3
- Pleos Vehicle SDK `2.3.2`는 `gradle/libs.versions.toml`의 `pleosVehicle`에서 관리하고, Maven 저장소는 `settings.gradle.kts`에 등록돼 있다. 인증은 필요 없다.
- `AndroidManifest.xml`에는 **쓰는 퍼미션만** 선언한다. Vehicle API 퍼미션은 Pleos App Market과 사전 협의·승인된 범위만 사용할 수 있고, 승인 없는 퍼미션은 앱 심사 탈락 사유다.
- 라이브러리 버전을 올릴 때는 `compileSdk 34`에서 빌드되는지 확인한다. (예: `core-ktx 1.15`는 compileSdk 35가 필요해서 1.13.1을 쓴다.)

### 막혔을 때

| 증상 | 확인할 것 |
|---|---|
| `JDK 17을 찾지 못했어요` | `brew install openjdk@17` 후 다시 실행 |
| 빌드 오류에 `25.0.3` 같은 Java 버전이 나온다 | Gradle이 JDK 25로 돌고 있다. JDK 17로 바꾼다. (`run.sh`는 자동으로 17을 쓴다) |
| `Command-line Tools가 없어요` | Android Studio > SDK Tools에서 Command-line Tools (latest) 설치 |
| 다운로드가 끊긴다 | 같은 명령을 다시 실행하면 이어받는다 |
| 체크섬이 맞지 않는다 | `~/.cache/altercharge/`의 zip을 지우고 다시 실행 |
| 디스크 부족 | 약 26GB 필요. 설치가 끝나면 내려받은 zip은 자동으로 지워진다 |
| 앱이 화면에 안 보인다 | `./scripts/run.sh`를 다시 실행한다. (메인 화면 0번에 띄운다) |
| 에뮬레이터가 느리다 | 다른 에뮬레이터와 무거운 앱을 끈다. RAM 4GB가 필요하다 |
| 위로도 해결 안 됨 | 오류 메시지 전체를 팀 채널에 올린다 |

<details>
<summary>수동 설치 (Windows이거나 스크립트가 안 될 때)</summary>

공식 가이드: [개발 환경 구성](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)

**A. Pleos 이미지 목록 등록** — Android Studio **Tools > SDK Manager > SDK Update Sites** 탭에서 `+`

- Name: `Pleos Connect System Image`
- URL (Mac M 시리즈): `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml`
- URL (Windows, Linux, Intel Mac): `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml`

**B. 이미지 설치 (약 4.7GB)**

1. SDK Manager > **SDK Platforms** 탭에서 **Android API 34**를 체크한다.
2. **Show Package Details**를 켜고 **Pleos Connect v2.1.0 APIs** 이미지를 체크한다.
3. Apply 후 **42dot SDK 라이선스**를 읽고 동의한 경우에만 진행한다.
4. 설치가 끝나면 Android Studio를 재시작한다.

**C. 가상 기기 만들기** — **Tools > Device Manager > Create device**

1. Form Factor: **Automotive** > **Pleos Connect v2.1.0** 선택
2. System image: **API 34 / Pleos Connect v2.1.0 APIs**
3. Show Advanced Settings: Default boot **Cold**, RAM **4 GB**

</details>

## 2. 협업 규칙 (기간이 짧아 해커톤처럼 빠르게)

자세한 내용은 [CONTRIBUTING.md](CONTRIBUTING.md). 요약:

- `main` 하나만 쓴다. 작업 브랜치는 `type/작업내용` 으로 짧게 따서 하루 안에 합친다.
- 이슈는 선택이다. **이슈가 있으면** 브랜치에 번호를 붙이고(`feat/12-charge-card`) PR 본문에 `Close #12`를 적는다. 없으면 번호 없이 진행한다.
- 커밋은 `type: 작업 내용` (한국어, scope 없음). 타입: `feat` `fix` `refactor` `style` `chore` `docs` `test`
- 작은 변경은 `main`에 바로 머지하고, 푸시 전에 `git pull --rebase` 한다.
- 공용 파일(Gradle, 매니페스트, 공통 컴포넌트)은 PR로 올리고 상대에게 알린다.
- 프론트 2명이므로 화면 단위로 담당을 나눠 같은 파일을 동시에 고치지 않는다.
- 키스토어, `local.properties`, 토큰, API 키는 커밋하지 않는다.

```bash
git switch main && git pull --rebase
git switch -c feat/charge-card        # 작업 시작
git commit -m "feat: 다음 충전일 카드 구현"
git pull --rebase && git push -u origin feat/charge-card
```

## 3. 공모전 제출 (접수 마감 2026-11-15)

[지원·제출 방법](https://appdevchallenge.auto-hmg.io/guide/submission) 기준으로 정리했다. 세부 내용은 공모전 사이트가 우선이다.

| 항목 | 내용 |
|---|---|
| 필수 | 접수양식(기획서)과 참가자 전원의 개인정보 동의서를 `appdevchallenge@hyundai-partners.com`으로 이메일 제출 |
| 가산점 | 시연 영상, GitHub 저장소(소스 + APK) |
| APK 위치 | 저장소의 `apk/` 폴더, 파일명 `팀명_앱이름_release.apk`, 실행 가능한 release 버전 |
| README | 앱 소개, 실행 방법, 테스트 환경, APK 위치를 적는다 |
| 비공개 저장소 | 공지된 심사용 계정을 Collaborator로 초대한다 |
| 수정 제출 | 마감 전에 처음 보낸 주소로 `[수정제출] ...` 제목으로 다시 보낸다 (마지막 제출본이 최종본) |

### release APK 서명 키

- 키스토어(`*.jks`, `*.keystore`)와 `keystore.properties`는 **저장소에 올리지 않는다.** (`.gitignore`로 제외됨)
- 보관 담당과 공유 방법: (미정 — 정해지면 여기에 적는다. 비밀번호는 문서에 적지 않는다.)
- APK는 한 명이 같은 키스토어로 만든다. 사람마다 다른 키로 서명하면 업데이트와 비교가 어긋난다.

## 4. 참고 문서

- [Pleos 개발 문서 시작하기](https://document.pleos.ai/docs/pleos-only/getting-started)
- [Pleos Connect SDK 사용 가이드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/)
- [SDK API 참조](https://document.pleos.ai/api-reference/connect-sdk-pleos)
- [Vehicle SDK 튜토리얼](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk)
- [차량 앱 개발 가이드(주행 규제, 오디오 포커스 등 정책)](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/intro)
- [공모전 사이트](https://appdevchallenge.auto-hmg.io/) — 접수 마감 2026.11.15, 1차 결과 11.23, 2차 PT 12.03~12.04

## 5. 아직 정해지지 않은 것

- 배터리 잔량(SOC)과 플러그·충전 상태를 읽는 정확한 API 이름 (Vehicle SDK의 `EvBattery` 도메인으로 보이며 API 참조에서 확인 필요)
- 주행 상태(주행 중/정차)를 판단하는 API (`CarUxRestrictions`는 문서에서 확인되지 않음. `DrivingMode`, NaviHelper 이벤트 후보)
- release 키스토어 보관 담당과 공유 방법
- 팀원 Pleos 팀 멤버 초대(프론트 2명, 백엔드)
- TTS, STT, FusedLocation 제공 시점(현재 Coming Soon)
- Android 버전 확정 여부: 현재 AGP 8.7.3, Kotlin 2.0.21, Gradle 8.9, JDK 17로 빌드됨. Pleos 문서에는 JDK, AGP, Kotlin 버전이 명시돼 있지 않음
