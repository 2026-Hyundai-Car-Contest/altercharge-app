# AlterCharge (Pleos Connect 차량용 앱)

Pleos Connect SDK + Android(Kotlin) 기반 전기차 충전 일정 앱. 공모전 제출용이며 속도가 우선이다.

## 범위

- 이 저장소는 프론트엔드(차량용 Android 앱)만 다룬다. 백엔드는 별도 저장소이며 여기서는 수정하지 않는다. 서버 응답 규격은 백엔드 쪽과 맞춘다.

## Git / 커밋 규칙

- 규칙의 원본은 `CONTRIBUTING.md`다. `main` 하나에 짧은 작업 브랜치(`type/작업내용`), 커밋은 `type: 작업 내용`(한국어, scope 없음).
- 커밋 메시지와 PR 본문에 `Co-Authored-By: Claude`, "Generated with Claude Code" 등 AI 작성 표시를 넣지 않는다. PR 본문에는 실제 변경 내용만 쓴다.
- 작은 변경은 `main`에 바로 머지해도 된다. 공용 파일(Gradle, 매니페스트, 공통 컴포넌트) 수정은 PR로 올린다.
- 커밋 전에 빌드가 통과하는지 확인한다. (Android 프로젝트 생성 후 명령을 여기에 적는다: 미정)
- 키스토어, `local.properties`, 토큰, API 키는 커밋하지 않는다.
- 푸시와 PR 생성은 사용자가 요청할 때만 한다.
