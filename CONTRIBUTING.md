# 협업 방법 (기간이 짧아 해커톤처럼 빠르게)

공모전 일정이 짧으므로 해커톤처럼 빠르게 합치는 것이 우선이다. 절차는 최소로 둔다.

## 브랜치

- `main` 하나만 쓴다. `develop`과 브랜치 보호는 없다.
- 작업은 `main`에서 짧은 브랜치를 따서 하고, 하루 안에 합친다.
- 이름: `type/작업내용` (영어 소문자, 하이픈). 이슈 번호는 선택.

```
feat/charge-card
fix/soc-null-crash
chore/initial-setup
```

타입: `feat` `fix` `refactor` `style` `chore` `docs` `test`

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
- 빌드가 깨진 채로 `main`에 두지 않는다. 푸시 전에 에뮬레이터에서 앱이 뜨는지 확인한다.

## 비밀값

키스토어, `local.properties`, 토큰, API 키는 커밋하지 않는다.
