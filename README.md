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

## 1. 환경 세팅 (처음 한 번, 약 1~2시간)

설치할 것이 많고 이미지 파일이 크다. **와이파이 환경에서, 디스크 여유 20GB 이상**을 확보하고 시작한다.

### 1-1. 설치 프로그램

| 필요한 것 | 비고 |
|---|---|
| [Android Studio](https://developer.android.com/studio) | **최신 버전.** 오래된 버전이면 먼저 업데이트한다. JDK는 Android Studio에 포함된 것을 쓰면 된다. |
| Git | 터미널에서 `git --version`이 나오면 된다. |
| 8GB 이상 여유 RAM | 에뮬레이터가 4GB를 쓴다. |

### 1-2. 저장소 받기

```bash
git clone https://github.com/2026-Hyundai-Car-Contest/altercharge-app.git
cd altercharge-app
git config core.hooksPath .githooks   # 커밋 메시지 검사 켜기 (꼭 실행)
```

### 1-3. Pleos Playground 가입과 프로젝트 신청

1. [Pleos Playground](https://developers.pleos.ai/playground)에서 가입하고 로그인한다. (오른쪽 위 "로그인")
2. [프로젝트 생성 및 SDK 사용 신청](https://document.pleos.ai/docs/pleos-only/create-my-project)을 따라 My Project를 만든다.
3. 서버 기반 TTS(`HYBRID`, `SERVER` 모드)를 쓰려면 이 프로젝트의 자격 증명이 필요하다. **자격 증명, 비밀번호는 코드와 저장소에 절대 넣지 않는다.** 필요하면 `secrets.properties`에 두면 `.gitignore`로 제외된다.
4. **프로젝트(`AlterCharge`)는 이미 생성돼 있다.** 팀원은 새로 만들지 않고, 가입한 이메일을 프로젝트 소유자에게 알려 팀 멤버로 초대받는다. (Playground > Workspace > My Project > 팀 관리)
5. 아래 SDK는 신청 없이 바로 쓸 수 있다: **Vehicle State, NaviHelper**
6. 아래 SDK는 2026-11-03 기준 **Coming Soon(신청 불가)** 이다: FusedLocation, TextToSpeech, SpeechToText, Vehicle Control. 음성과 거점 진입 감지는 제공되기 전까지 화면과 버튼 대체 방식으로 만든다.

### 1-4. 에뮬레이터 만들기

공식 가이드: [개발 환경 구성](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)

**A. Pleos 이미지 목록 등록** — Android Studio 메뉴 **Tools > SDK Manager > SDK Update Sites** 탭에서 `+` 버튼

- Name: `Pleos Connect System Image`
- URL (Mac M1/M2/M3, Apple Silicon): `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml`
- URL (Windows, Linux, Intel Mac): `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml`

> Mac이 M 시리즈인지 모르겠으면 왼쪽 위 Apple 메뉴 > 이 Mac에 관하여 에서 "칩"을 확인한다.

**B. 이미지 설치 (약 4.7GB)**

1. SDK Manager > **SDK Platforms** 탭에서 **Android API 34**를 체크한다.
2. 오른쪽 아래 **Show Package Details**를 켠 뒤, API 34 아래에 보이는 **Pleos Connect v2.1.0 APIs** 이미지를 체크한다.
3. Apply를 누르면 **42dot SDK 라이선스 동의 창**이 나온다. 내용을 읽고 본인이 동의한 경우에만 진행한다.
4. 설치가 끝나면 **Android Studio를 재시작**한다.

> 이전 버전의 Pleos 이미지가 있으면 삭제하고 다시 설치하라고 문서에 나와 있다.

**C. 가상 기기 만들기**

1. **Tools > Device Manager > Create device**
2. Form Factor: **Automotive** 탭에서 **Pleos Connect v2.1.0** 선택 > Next
3. System image: **API 34 / Pleos Connect v2.1.0 APIs** 선택 > Next
4. **Show Advanced Settings**에서 다음을 바꾼다.
   - Default boot: **Cold**
   - RAM: **4 GB**
5. Finish. 목록에 생긴 기기의 ▶ 버튼으로 켠다. 처음 부팅은 몇 분 걸린다.

차량 화면이 뜨면 성공이다.

### 1-5. 프로젝트 열기 / 만들기

- **프로젝트가 이미 저장소에 있으면**: Android Studio에서 `altercharge-app` 폴더를 연다(Open). Gradle 동기화가 끝날 때까지 기다린다.
- **아직 없으면 (최초 1명만 만든다)**: New Project > **Car > Empty Activity**, 언어 Kotlin, Minimum SDK **API 28** 이상으로 만들고 이 폴더에 둔 뒤 푸시한다. 나머지 팀원은 이 단계를 하지 않고 clone만 한다.

SDK 추가 설정(프로젝트를 만든 사람이 한 번만 한다):

`settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://nexus-playground.pleos.ai/repository/maven-releases/") }
    }
}
```

`app/build.gradle.kts`

```kotlin
implementation("ai.pleos.playground:Vehicle:<버전>")
```

문서마다 버전이 2.3.1과 2.3.2로 달라서 [Vehicle SDK 문서](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)의 최신 버전을 쓴다.

`AndroidManifest.xml`에는 **쓰는 퍼미션만** 선언한다. Vehicle API 퍼미션은 Pleos App Market과 사전 협의·승인된 범위만 사용할 수 있고, 승인 없는 퍼미션은 앱 심사 탈락 사유다.

### 1-6. 실행 확인

1. 위에서 만든 Automotive 에뮬레이터를 켠다.
2. Android Studio 위쪽에서 실행 기기로 그 에뮬레이터를 고르고 ▶ Run 한다.
3. 앱이 차량 화면에 뜨면 세팅 완료.

### 막혔을 때

| 증상 | 확인할 것 |
|---|---|
| Pleos 이미지가 목록에 안 보인다 | Update Sites URL을 정확히 넣었는지(Mac M 시리즈는 `crp-arm64`), Show Package Details를 켰는지, Android Studio를 재시작했는지 |
| Device Manager에 "Pleos Connect"가 없다 | Form Factor가 Automotive인지, 이미지 설치가 끝났는지 |
| 에뮬레이터가 안 켜지거나 느리다 | RAM 4GB, Default boot Cold인지, 다른 에뮬레이터를 꺼 뒀는지 |
| 의존성을 못 받는다(Could not resolve) | `settings.gradle.kts`에 Maven 주소가 있는지, 와이파이 연결, Maven 저장소 접근 권한 |
| 빌드가 minSdk 오류 | Minimum SDK가 28 이상인지 |
| 위로도 해결 안 됨 | 오류 메시지 전체를 팀 채널에 올린다 |

## 2. 협업 규칙 (기간이 짧아 해커톤처럼 빠르게)

자세한 내용은 [CONTRIBUTING.md](CONTRIBUTING.md). 요약:

- `main` 하나만 쓴다. 작업 브랜치는 `type/작업내용` 으로 짧게 따서 하루 안에 합친다.
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
- 빌드·lint 명령(프로젝트 생성 후 `CLAUDE.md`에 기록)
- JDK, AGP, Kotlin 버전(문서에 명시 없음. 프로젝트 생성 후 기록)
