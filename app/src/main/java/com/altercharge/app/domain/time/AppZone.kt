/** 기기 시간대 대신 쓴다: 에뮬레이터는 UTC일 수 있어 날짜가 9시간 어긋난다 */
package com.altercharge.app.domain.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor

val KoreaZone: ZoneId = ZoneId.of("Asia/Seoul")

/** 한국 날짜. LocalDate.ofInstant는 API 34부터라 못 쓴다 */
fun Instant.toKoreaDate(): LocalDate = atZone(KoreaZone).toLocalDate()

/** 저장해 둔 시각 글자를 읽는다. 비었거나 깨졌으면 null (저장 전처럼 다룬다) */
fun String.toInstantOrNull(): Instant? = runCatching { Instant.parse(this) }.getOrNull()

/**
 * 기기 언어의 짧은 시각. 한국어는 시안 표기 "오후 1:45", 영어는 "1:45 PM"이 된다.
 * 형식은 만들 때의 기본 언어를 잡으므로 언어를 바꿔도 따라가게 부를 때마다 만든다.
 * 영어 형식은 AM/PM 앞에 좁은 공백(U+202F)이 들어가 글꼴에 따라 깨져 보여서 보통 공백으로 바꾼다.
 */
fun formatShortTime(time: TemporalAccessor): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(time).replace('\u202F', ' ')
