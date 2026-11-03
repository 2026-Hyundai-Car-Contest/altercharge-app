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

- 작은 변경(파일 몇 개, 자기 화면 안): `main`에 바로 머지하거나 푸시한다. 푸시 전에 `git pull --rebase`.
- 큰 변경이나 공용 파일(Gradle, 매니페스트, 공통 컴포넌트) 수정: PR을 올리고 팀원에게 알린다. 승인 대기로 막지 않고, 확인되면 Squash and Merge.
- 프론트 2명이므로 **화면 단위로 담당을 나눠** 같은 파일을 동시에 고치지 않는다. 공용 파일을 건드릴 때는 상대에게 먼저 알린다.
- 상대가 PR을 올리면 확인만 하고 바로 머지해도 된다. 서로 승인을 기다리느라 멈추지 않는다.
- 빌드가 깨진 채로 `main`에 두지 않는다. 푸시 전에 `./scripts/check.sh`(lint와 빌드)를 돌리고, 화면을 바꿨다면 `./scripts/run.sh`로 에뮬레이터에서 확인한다.

## 제출용 APK

- 제출용 release APK는 `apk/팀명_앱이름_release.apk`로 둔다. 한 명이 같은 키스토어로 만들어 올린다. (`./scripts/release.sh <팀명>`이 이 이름과 위치로 만들어 준다.)
- 수동으로 만든 APK는 `apk/` 밖에 두지 않는다. (`.gitignore`는 `apk/` 안의 APK만 허용한다.)

## 비밀값

키스토어, `local.properties`, 토큰, API 키, Pleos 클라이언트 ID/시크릿은 커밋하지 않는다.
