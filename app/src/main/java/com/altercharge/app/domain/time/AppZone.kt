/** 기기 시간대 대신 쓴다: 에뮬레이터는 UTC일 수 있어 날짜가 9시간 어긋난다 */
package com.altercharge.app.domain.time

import java.time.ZoneId

val KoreaZone: ZoneId = ZoneId.of("Asia/Seoul")
