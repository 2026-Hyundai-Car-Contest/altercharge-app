package com.altercharge.app.ui.card.vehiclestatus

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import java.util.Locale

// ---------------------------------------------------------------------------
// 상황별 내용
// ---------------------------------------------------------------------------

/** @param summary WF 요약 줄(info_summary)에 들어가는 글. 기본은 "라벨 값 단위" */
@Immutable
internal data class Tile(
    val label: String,
    val value: String,
    val unit: String?,
    val summary: String = listOfNotNull(label, value, unit).joinToString(" "),
)

internal sealed interface StatusLine {
    data class Plain(val text: String) : StatusLine

    /** template의 %1$s, %2$s 자리에 args가 들어가고, 그 숫자만 강조된다. */
    data class TimeLeft(val template: String, val args: List<String>) : StatusLine

    /** 상태 문구 없음. 09 배터리 위험 · 미연결은 남은 시간 문구를 빼기로 함(기획 · 디자이너 확정, 2026-11-10) */
    data object None : StatusLine
}

@Immutable
internal data class CardContent(
    val headline: String,
    val portraitHeadline: String,
    val status: StatusLine,
    val accent: Color,
    val gaugeLevel: Int,
    val tiles: List<Tile>,
    val portraitTiles: List<Tile>,
    val landscapeImage: VehicleImageKind,
    /** WF 요약 줄 */
    val summary: String,
) {
    /** 충전기 쪽(연결 · 대기 · 오류) 스테이지인지. 세로 · tall은 이 값으로 차 자리 크기가 바뀐다 */
    val isCharging: Boolean get() = landscapeImage == VehicleImageKind.CHARGING
}

/** 상황별 문구 · 색 · 타일을 만든다. 리소스 문자열을 읽어야 해서 Composable이다. */
@Composable
internal fun buildContent(state: CardVehicleStatusUiState, mode: CardVehicleStatusMode): CardContent {
    val colors = AlterTheme.colors
    val situation = state.situation
    val unknown = stringResource(R.string.card_vehicle_status_value_unknown)
    val km = stringResource(R.string.card_vehicle_status_unit_km)
    val expected = stringResource(R.string.card_vehicle_status_unit_expected)
    val done = stringResource(R.string.card_vehicle_status_unit_done)

    val rangeTile = Tile(
        label = stringResource(R.string.card_vehicle_status_tile_range),
        value = state.rangeKm?.toString() ?: unknown,
        unit = km,
    )
    val fullAtLabel = stringResource(R.string.card_vehicle_status_tile_full_at)
    // WF 요약은 "21:40 충전 완료" 순서다 (366:39036 info_summary)
    val fullAtValue = state.fullAt ?: unknown
    val fullAtSummary = stringResource(R.string.card_vehicle_status_summary_full_at, fullAtValue)
    val secondTile: Tile? = when (situation) {
        VehicleSituation.CHARGE_RECOMMENDED -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_nearest_station),
            value = state.nearestStationKm?.let { String.format(Locale.ROOT, "%.1f", it) } ?: unknown,
            unit = km,
        )
        VehicleSituation.READY -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_last_charge),
            value = state.lastChargedAt ?: unknown,
            unit = done,
        )
        VehicleSituation.CHARGING,
        VehicleSituation.CHARGING_LOW,
        VehicleSituation.CHARGING_CRITICAL,
        VehicleSituation.SCHEDULED,
        -> Tile(fullAtLabel, fullAtValue, expected, fullAtSummary)
        VehicleSituation.CHARGED -> Tile(fullAtLabel, fullAtValue, done, fullAtSummary)
        // 자동 재개 시각은 MVP 제외(REQ_VS_007). 목업 값이 없으면(수동 재개 포함) "–"로 둔다.
        VehicleSituation.PAUSED -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_auto_resume),
            value = state.autoResumeAt ?: unknown,
            unit = if (state.autoResumeAt != null) expected else null,
        )
        VehicleSituation.CRITICAL_UNPLUGGED -> null
        VehicleSituation.DEPLETED -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_emergency),
            value = stringResource(R.string.card_vehicle_status_tile_emergency_value),
            unit = stringResource(R.string.card_vehicle_status_unit_24h),
        )
        VehicleSituation.CHARGE_ERROR -> null
        VehicleSituation.NO_DATA -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_retry),
            value = state.retryInSeconds?.toString() ?: unknown,
            unit = stringResource(R.string.card_vehicle_status_unit_seconds_later),
        )
    }
    val tiles = if (situation == VehicleSituation.CHARGE_ERROR) {
        // 11 충전 오류: 원인 · 조치 타일 (REQ_EX_002). 종류를 모르면 값은 "–"
        val fault = state.fault?.let { faultText(it) }
        listOf(
            Tile(
                label = stringResource(R.string.card_vehicle_status_tile_cause),
                value = fault?.cause ?: unknown,
                unit = null,
            ),
            Tile(
                label = stringResource(R.string.card_vehicle_status_tile_action),
                value = fault?.action ?: unknown,
                unit = fault?.actionUnit,
            ),
        )
    } else {
        listOfNotNull(rangeTile, secondTile)
    }
    // 세로 시안은 01 · 02 · 09 · 10 · 12에서 주행 가능 타일 하나만 보여준다.
    val portraitTiles = when (situation) {
        VehicleSituation.CHARGE_RECOMMENDED,
        VehicleSituation.READY,
        VehicleSituation.CRITICAL_UNPLUGGED,
        VehicleSituation.DEPLETED,
        VehicleSituation.NO_DATA,
        -> listOf(rangeTile)
        else -> tiles
    }

    val name = state.driverName
    val headline = when (situation) {
        VehicleSituation.CHARGE_RECOMMENDED -> stringResource(R.string.card_vehicle_status_headline_charge_recommended, name)
        VehicleSituation.READY -> stringResource(R.string.card_vehicle_status_headline_ready, name)
        VehicleSituation.CHARGING -> stringResource(R.string.card_vehicle_status_headline_charging, name)
        VehicleSituation.CHARGING_LOW -> stringResource(R.string.card_vehicle_status_headline_charging_low)
        VehicleSituation.CHARGING_CRITICAL -> stringResource(R.string.card_vehicle_status_headline_charging_critical)
        VehicleSituation.CHARGED -> stringResource(R.string.card_vehicle_status_headline_charged, name)
        VehicleSituation.PAUSED -> stringResource(R.string.card_vehicle_status_headline_paused)
        VehicleSituation.SCHEDULED -> stringResource(
            R.string.card_vehicle_status_headline_scheduled,
            state.scheduledAt ?: unknown,
        )
        VehicleSituation.CRITICAL_UNPLUGGED -> stringResource(R.string.card_vehicle_status_headline_critical_unplugged)
        VehicleSituation.DEPLETED -> stringResource(R.string.card_vehicle_status_headline_depleted)
        VehicleSituation.CHARGE_ERROR -> stringResource(R.string.card_vehicle_status_headline_charge_error)
        VehicleSituation.NO_DATA -> stringResource(R.string.card_vehicle_status_headline_no_data)
    }
    // 세로(portrait)와 W-Standard 세로(portrait_compact)는 헤드라인 칸이 카드 폭 절반이라 짧은 문구를 쓴다
    val portraitHeadline = if (mode == CardVehicleStatusMode.PORTRAIT || mode == CardVehicleStatusMode.PORTRAIT_COMPACT) {
        portraitHeadlineRes(situation)?.let { stringResource(it) } ?: headline
    } else {
        headline
    }

    val status: StatusLine = when (situation) {
        VehicleSituation.CHARGE_RECOMMENDED ->
            StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_charge_recommended))
        VehicleSituation.READY -> StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_not_connected))
        VehicleSituation.CHARGING,
        VehicleSituation.CHARGING_LOW,
        VehicleSituation.CHARGING_CRITICAL,
        -> timeLeft(state.remainingMinutes, unknown)
        // 09는 상태 문구 없음 (StatusLine.None 설명 참고)
        VehicleSituation.CRITICAL_UNPLUGGED -> StatusLine.None
        VehicleSituation.CHARGED -> StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_charged))
        VehicleSituation.PAUSED -> StatusLine.Plain(
            when (state.pauseReason) {
                PauseReason.GRID_DEMAND -> stringResource(R.string.card_vehicle_status_status_paused_grid_demand)
                // 일시정지 사유는 MVP 제외(REQ_VS_007). 값이 없으면 "–"
                null -> unknown
            },
        )
        VehicleSituation.SCHEDULED -> StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_scheduled))
        VehicleSituation.DEPLETED -> StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_depleted))
        VehicleSituation.CHARGE_ERROR -> StatusLine.Plain(
            state.fault?.let { faultText(it).status }
                // TODO 디자이너 확인: 오류 종류를 모를 때 문구가 시안에 없다.
                ?: unknown,
        )
        VehicleSituation.NO_DATA -> StatusLine.Plain(
            stringResource(R.string.card_vehicle_status_status_last_updated, state.lastUpdatedAt ?: unknown),
        )
    }

    val landscapeImage = when (state.chip) {
        ConnectionChip.CONNECTED, ConnectionChip.STANDBY, ConnectionChip.ERROR -> VehicleImageKind.CHARGING
        ConnectionChip.UNPLUGGED, ConnectionChip.OFFLINE -> VehicleImageKind.DRIVING
    }

    return CardContent(
        headline = headline,
        portraitHeadline = portraitHeadline,
        status = status,
        accent = accentColor(situation, colors),
        gaugeLevel = gaugeLevel(state.socPercent),
        tiles = tiles,
        portraitTiles = portraitTiles,
        landscapeImage = landscapeImage,
        // 항목 안 공백은 줄바꿈 없는 공백으로 바꿔 "충전 완료"처럼 한 항목은 붙여 두고, 줄은 항목 사이에서만 바뀌게 한다
        summary = tiles.joinToString(SUMMARY_SEPARATOR) { it.summary.replace(' ', NO_BREAK_SPACE) },
    )
}

@StringRes
private fun portraitHeadlineRes(situation: VehicleSituation): Int? = when (situation) {
    VehicleSituation.CHARGE_RECOMMENDED -> R.string.card_vehicle_status_portrait_headline_charge_recommended
    VehicleSituation.CHARGING_CRITICAL -> R.string.card_vehicle_status_portrait_headline_charging_critical
    VehicleSituation.DEPLETED -> R.string.card_vehicle_status_portrait_headline_depleted
    VehicleSituation.CHARGE_ERROR -> R.string.card_vehicle_status_portrait_headline_charge_error
    VehicleSituation.NO_DATA -> R.string.card_vehicle_status_portrait_headline_no_data
    else -> null
}

@Composable
private fun timeLeft(minutes: Int?, unknown: String): StatusLine {
    if (minutes == null) {
        return StatusLine.TimeLeft(stringResource(R.string.card_vehicle_status_time_left_minutes), listOf(unknown))
    }
    // 음수(데이터 오류)는 0분으로 보여 준다.
    val safeMinutes = minutes.coerceAtLeast(0)
    val hours = safeMinutes / MINUTES_PER_HOUR
    val rest = safeMinutes % MINUTES_PER_HOUR
    return if (hours > 0) {
        StatusLine.TimeLeft(
            stringResource(R.string.card_vehicle_status_time_left_hours),
            listOf(hours.toString(), rest.toString()),
        )
    } else {
        StatusLine.TimeLeft(stringResource(R.string.card_vehicle_status_time_left_minutes), listOf(rest.toString()))
    }
}

/** template의 %n$s 자리를 args로 채우고, 채운 글자에만 emphasis를 준다. */
internal fun emphasizeArgs(
    template: String,
    args: List<String>,
    emphasis: SpanStyle,
    normal: SpanStyle,
): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    ArgPattern.findAll(template).forEach { match ->
        withStyle(normal) { append(template.substring(cursor, match.range.first)) }
        val index = match.groupValues[1].toInt() - 1
        withStyle(emphasis) { append(args.getOrElse(index) { "" }) }
        cursor = match.range.last + 1
    }
    withStyle(normal) { append(template.substring(cursor)) }
}

private val ArgPattern = Regex("%(\\d+)\\$[sd]")
private const val MINUTES_PER_HOUR = 60

/** WF 요약 줄에서 타일 사이 구분 */
/** 앞 공백은 줄바꿈 없는 공백이라 "·"가 줄 맨 앞에 오지 않는다 */
private const val SUMMARY_SEPARATOR = "\u00A0· "
private const val NO_BREAK_SPACE = '\u00A0'
