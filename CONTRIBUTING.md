# 협업 방법 (기간이 짧아 해커톤처럼 빠르게)

공모전 일정이 짧으므로 해커톤처럼 빠르게 합치는 것이 우선이다. 절차는 최소로 둔다.

## 브랜치

- `main` 하나만 쓴다. `develop`과 브랜치 보호는 없다.
- 작업은 `main`에서 짧은 브랜치를 따서 하고, 하루 안에 합친다.
- 이름: `type/작업내용` (영어 소문자, 하이픈). **이슈가 있으면 번호를 붙인다:** `type/이슈번호-작업내용`
- 타입: `feat` `fix` `refactor` `style` `chore` `docs` `test` (커밋, PR 제목도 같은 타입을 쓴다)

```
feat/12-charge-card      # 이슈 #12가 있을 때
fix/20-soc-null-crash
chore/initial-setup      # 이슈가 없을 때
```

## 이슈 연결

이슈는 선택이지만, **이슈가 있으면 번호를 연결한다.**

- 브랜치 이름에 번호를 붙인다. (`feat/12-charge-card`)
- PR 본문의 Related Issue에 `Close #12`를 적는다. PR이 머지되면 이슈가 자동으로 닫힌다.
- PR 제목과 커밋 메시지에는 번호를 쓰지 않는다. (Squash and Merge 때 PR 번호가 자동으로 붙는다.)
- 이슈 없이 작업한 PR은 Related Issue 칸을 비워 둔다.

## 커밋

```
type: 작업 내용
```

- 한국어, scope 없음. `.githooks/commit-msg`가 형식을 검사한다.
- clone 후 한 번 실행: `git config core.hooksPath .githooks`

```
feat: 다음 충전일 카드 구현
fix: 배터리 잔량이 없을 때 앱이 종료되는 오류 수정
```

## 합치는 방법

- **모든 변경은 PR로 올리고 Squash and Merge로 합친다.** 작은 변경도 같다. `main`에 직접 푸시하지 않는다. (GitHub 무료 요금제의 비공개 저장소라 브랜치 보호를 쓸 수 없어서, `.githooks/pre-push` 훅이 직접 푸시를 막는다. 정말 필요한 경우에만 `ALLOW_MAIN_PUSH=1 git push`로 우회한다.) PR 하나가 커밋 하나로 합쳐져 `main`의 기록이 깨끗하게 남는다. PR 제목이 최종 커밋 메시지가 되므로 `type: 작업 내용` 형식으로 쓴다.
- **승인을 기다리지 않는다.** 상대가 올린 PR은 확인만 하고 바로 머지해도 되고, 자기 화면 안의 작은 PR은 CI가 통과하면 스스로 머지해도 된다.
- 프론트 2명이므로 **화면 단위로 담당을 나눠** 같은 파일을 동시에 고치지 않는다. 공용 파일(Gradle, 매니페스트, 공통 컴포넌트)을 건드릴 때는 상대에게 먼저 알린다.
- 브랜치는 하루 안에 합치고, 푸시 전에 `git pull --rebase` 한다.
- 빌드가 깨진 채로 `main`에 두지 않는다. 푸시 전에 `./scripts/check.sh`(lint와 빌드)를 돌리고, 화면을 바꿨다면 `./scripts/run.sh`로 에뮬레이터에서 확인한다.

## 자동으로 되는 것

| 무엇 | 언제 |
|---|---|
| 커밋 메시지 형식, AI 표시 검사 (`commit-msg` 훅) | 커밋할 때 (로컬) |
| `main` 직접 푸시 차단 (`pre-push` 훅) | 푸시할 때 (로컬) |
| 빌드와 lint (CI) | PR을 열거나 `main`에 푸시할 때 |
| 담당자 지정, 리뷰어 요청(프론트 두 명이 서로) | 이슈나 PR을 열 때 |
| 라벨, 브랜치 이름·PR 제목 검사 | PR을 열거나 제목을 고칠 때 (실패해도 병합은 가능) |
| 디버그 APK 보관 | `main`에 병합될 때 (Actions 결과물 `app-debug-apk`, 30일) |
| 병합 후 브랜치 삭제 | PR 병합 후 |

CI 결과는 GitHub 저장소의 **Actions 탭**에서 본다. 리뷰어 후보는 `.github/workflows/auto-assign.yml`의 `CANDIDATES`에서 관리한다.

## 제출용 APK

- 제출용 release APK는 `apk/팀명_앱이름_release.apk`로 둔다. 한 명이 같은 키스토어로 만들어 올린다. (`./scripts/release.sh <팀명>`이 이 이름과 위치로 만들어 준다.)
- 수동으로 만든 APK는 `apk/` 밖에 두지 않는다. (`.gitignore`는 `apk/` 안의 APK만 허용한다.)

## 비밀값

키스토어, `local.properties`, 토큰, API 키, Pleos 클라이언트 ID/시크릿은 커밋하지 않는다.
