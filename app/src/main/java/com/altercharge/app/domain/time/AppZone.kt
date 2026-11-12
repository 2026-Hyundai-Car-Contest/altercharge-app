/**
 * 앱이 날짜를 자르는 시간대. 서버도 한국시간 날짜로 보관하고, 판정(하루 단위) · 주간 기록 · "N분 전 기준" 날짜가 모두 이 값을 쓴다.
 * 기기 시간대(ZoneId.systemDefault)를 쓰지 않는다: 에뮬레이터는 UTC일 수 있어 날짜가 9시간 어긋난다.
 * 홈 화면에서 오늘 날짜는 LocalDate.now(KoreaZone)으로 구한다.
 */
package com.altercharge.app.domain.time

import java.time.ZoneId

val KoreaZone: ZoneId = ZoneId.of("Asia/Seoul")
