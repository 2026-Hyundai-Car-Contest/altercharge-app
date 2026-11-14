/**
 * 기기에 충전소 방문 횟수를 센다 (REQ_ST_008).
 * 서버는 MOCK 이벤트를 실제 방문으로 세지 않아 시연 중에는 이 기록이 화면 값이 되고,
 * 실제 방문이 서버에 쌓이면 서버 값과 큰 쪽을 쓴다 (StationQuery.visitCountOf).
 */
package com.altercharge.app.data.local

import com.altercharge.app.domain.time.KoreaZone
import java.time.LocalDate
import java.time.ZoneId

class VisitCountStore(
    private val store: KeyValueStore,
    private val zone: ZoneId = KoreaZone,
) {
    private val lock = Any()

    /** 충전소별 총 방문 횟수 */
    fun counts(): Map<String, Int> = synchronized(lock) { load().mapValues { it.value.total } }

    fun count(stationId: String): Int = counts()[stationId] ?: 0

    /**
     * 오늘 처음 방문한 충전소면 1 늘리고 새 횟수를 돌려준다. 같은 날 다시 방문했으면 null (하루 1회).
     * 화면은 이 값을 바로 써서 즉시 +1을 보여 준다 (REQ_ST_008 예외).
     */
    fun recordIfFirstToday(stationId: String, date: LocalDate = today()): Int? = synchronized(lock) {
        val saved = load().toMutableMap()
        val entry = saved[stationId] ?: VisitEntry(total = 0, lastDate = null)
        if (entry.lastDate == date) return null
        val updated = entry.copy(total = entry.total + 1, lastDate = date)
        saved[stationId] = updated
        store.putStrings(mapOf(KEY_VISITS to encodeVisits(saved)))
        updated.total
    }

    private fun load(): Map<String, VisitEntry> = decodeVisits(store.getString(KEY_VISITS))

    private fun today(): LocalDate = LocalDate.now(zone)

    private companion object {
        const val KEY_VISITS = "station_visits"
    }
}

/** 충전소 하나의 방문 기록. lastDate는 하루 1회를 가르는 마지막으로 센 날이다 */
data class VisitEntry(val total: Int, val lastDate: LocalDate?)

private const val ENTRY_SEPARATOR = "\n"
private const val FIELD_SEPARATOR = "\u001F"

/** "충전소ID · 총 횟수 · 마지막 날짜"를 한 줄씩. 날짜가 없으면 빈 칸 */
internal fun encodeVisits(entries: Map<String, VisitEntry>): String = entries.entries
    .sortedBy { it.key }
    .joinToString(ENTRY_SEPARATOR) { (stationId, entry) ->
        listOf(stationId, entry.total.toString(), entry.lastDate?.toString().orEmpty())
            .joinToString(FIELD_SEPARATOR)
    }

/** 깨진 줄은 버린다: 방문 기록 하나 때문에 화면이 멈추지 않게 */
internal fun decodeVisits(text: String?): Map<String, VisitEntry> {
    if (text.isNullOrBlank()) return emptyMap()
    return text.split(ENTRY_SEPARATOR).mapNotNull { line ->
        val fields = line.split(FIELD_SEPARATOR)
        if (fields.size != 3) return@mapNotNull null
        val stationId = fields[0]
        val total = fields[1].toIntOrNull() ?: return@mapNotNull null
        val lastDate = fields[2].takeIf { it.isNotEmpty() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (stationId.isEmpty() || total < 0) null else stationId to VisitEntry(total, lastDate)
    }.toMap()
}
