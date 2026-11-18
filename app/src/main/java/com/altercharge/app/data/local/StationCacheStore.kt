/**
 * 받아 둔 주변 충전소 목록 (REQ_CM_006).
 * 연결이 끊겨도 목록 · 정렬 · 필터를 쓸 수 있게 마지막 목록과 받은 시각을 저장한다.
 * 유효 기간(REQ_CM_009)은 읽는 쪽이 받은 시각으로 판단한다.
 */
package com.altercharge.app.data.local

import com.altercharge.app.data.enumOrNull
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.domain.time.toInstantOrNull
import java.time.Instant

data class CachedStations(val stations: List<StationSummary>, val receivedAt: Instant)

class StationCacheStore(private val store: KeyValueStore) {

    fun save(stations: List<StationSummary>, receivedAt: Instant) {
        store.putStrings(
            mapOf(
                KEY_STATIONS to encodeStations(stations),
                KEY_RECEIVED_AT to receivedAt.toString(),
            ),
        )
    }

    /** 목록이나 받은 시각이 없으면 null */
    fun load(): CachedStations? {
        val receivedAt = store.getString(KEY_RECEIVED_AT)?.toInstantOrNull()
            ?: return null
        val text = store.getString(KEY_STATIONS) ?: return null
        return CachedStations(decodeStations(text), receivedAt)
    }

    private companion object {
        const val KEY_STATIONS = "station_cache"
        const val KEY_RECEIVED_AT = "station_cache_received_at"
    }
}

private const val ROW_SEPARATOR = "\n"
private const val FIELD_SEPARATOR = "\u001F"
private const val TYPE_SEPARATOR = ","

internal fun encodeStations(stations: List<StationSummary>): String = stations.joinToString(ROW_SEPARATOR) { s ->
    listOf(
        s.stationId,
        s.name,
        s.address,
        s.lat.toString(),
        s.lng.toString(),
        s.distanceMeters.toString(),
        s.availableCount.toString(),
        s.totalValidCount.toString(),
        s.chargerTypes.joinToString(TYPE_SEPARATOR) { it.name },
        s.maxOutputKw?.toString().orEmpty(),
        s.visitCount?.toString().orEmpty(),
        s.statusObservedAt?.toString().orEmpty(),
    ).joinToString(FIELD_SEPARATOR)
}

/** 깨진 줄은 버린다: 저장값 하나 때문에 목록 전체가 사라지지 않게 */
internal fun decodeStations(text: String): List<StationSummary> {
    if (text.isBlank()) return emptyList()
    return text.split(ROW_SEPARATOR).mapNotNull { line ->
        val f = line.split(FIELD_SEPARATOR)
        if (f.size != 12) return@mapNotNull null
        StationSummary(
            stationId = f[0],
            name = f[1],
            address = f[2],
            lat = f[3].toDoubleOrNull() ?: return@mapNotNull null,
            lng = f[4].toDoubleOrNull() ?: return@mapNotNull null,
            distanceMeters = f[5].toIntOrNull() ?: return@mapNotNull null,
            availableCount = f[6].toIntOrNull() ?: return@mapNotNull null,
            totalValidCount = f[7].toIntOrNull() ?: return@mapNotNull null,
            chargerTypes = f[8].split(TYPE_SEPARATOR)
                .filter { it.isNotEmpty() }
                .map { name -> enumOrNull<ConnectorType>(name) ?: ConnectorType.UNKNOWN },
            maxOutputKw = f[9].toDoubleOrNull(),
            visitCount = f[10].toIntOrNull(),
            statusObservedAt = f[11].toInstantOrNull(),
        )
    }
}
