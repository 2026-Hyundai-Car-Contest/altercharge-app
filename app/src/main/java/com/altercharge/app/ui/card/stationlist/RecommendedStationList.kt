package com.altercharge.app.ui.card.stationlist

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.station.StationFilter
import com.altercharge.app.data.station.StationSort
import com.altercharge.app.domain.time.KoreaZone
import com.altercharge.app.ui.card.info.LastChecked
import com.altercharge.app.ui.card.info.lastCheckedText
import com.altercharge.app.ui.card.station.ChargingType
import com.altercharge.app.ui.card.stationitem.CardItemUiState
import com.altercharge.app.ui.card.stationitem.StationItem
import com.altercharge.app.ui.common.searchbar.SearchBar
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.station.StationListMessage
import com.altercharge.app.ui.station.StationListUiState
import com.altercharge.app.ui.station.VoiceSearchState
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** 미리보기용 값. 실제 화면은 ViewModel이 만든 state를 넘긴다 */
private val previewStations = listOf(
    CardItemUiState(
        stationId = "ST-001",
        stationName = "홍대입구역 공영주차장",
        availableChargerCount = 2,
        totalChargerCount = 4,
        chargingType = ChargingType.DC,
        distance = 0.6,
        chargeStationAddress = "서울 마포구 양화로 160",
        connectorTypes = listOf(ConnectorType.DC_COMBO),
        visitCount = 3,
    ),
    CardItemUiState(
        stationId = "ST-002",
        stationName = "연남동 공영주차장",
        availableChargerCount = 3,
        totalChargerCount = 3,
        chargingType = ChargingType.DC,
        distance = 1.2,
        chargeStationAddress = "서울 마포구 동교로 263",
        connectorTypes = listOf(ConnectorType.DC_CHADEMO, ConnectorType.AC_3PHASE),
    ),
    CardItemUiState(
        stationId = "ST-003",
        stationName = "연희동 주민센터",
        availableChargerCount = 1,
        totalChargerCount = 1,
        chargingType = ChargingType.AC,
        distance = 1.8,
        chargeStationAddress = "서울 서대문구 연희로 140",
        connectorTypes = listOf(ConnectorType.AC_SLOW),
        visitCount = 1,
    ),
)

/**
 * R2 주변 충전소 목록 (REQ_SL_001 ~ REQ_SL_005).
 * 검색 · 정렬 · 필터는 [state]의 값을 따르고, 바뀌면 콜백으로 알린다 (값은 ViewModel이 만든다).
 */
@Composable
fun RecommendedStationList(
    modifier: Modifier = Modifier,
    state: StationListUiState,
    onQueryChange: (String) -> Unit = {},
    onMicClick: () -> Unit = {},
    onSortChange: (StationSort) -> Unit = {},
    onFilterChange: (StationFilter) -> Unit = {},
    onStationClick: (CardItemUiState) -> Unit = {},
    /** R1에 대신 보여 주는 충전소 (REQ_SL_006). 목록에서 강조한다 */
    selectedStationId: String? = null,
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(AlterRadius.XXLarge))
            // Figma bg_glow: 회색 바탕 위에 아래로 갈수록 진해지는 blue_100(55%). 바탕이 blue_50이면 선택 칩 배경이 묻힌다
            .background(AlterTheme.colors.containerBackgroundSubtle)
            .background(
                Brush.verticalGradient(
                    0f to AlterTheme.colors.blue100.copy(alpha = 0f),
                    0.55f to AlterTheme.colors.blue100.copy(alpha = 0.35f * 0.55f),
                    1f to AlterTheme.colors.blue100.copy(alpha = 0.55f),
                ),
            )
            .padding(AlterMargin.Normal),
    ) {
        // 타이틀. 오프라인으로 저장한 목록이면 옆에 "N분 전 기준" (Figma header · title_row · last_checked, REQ_CM_006)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.station_list_title),
                style = AlterTheme.typography.bodyXSmall,
                color = AlterTheme.colors.textPrimary,
                textAlign = TextAlign.Left,
                modifier = Modifier.alignByBaseline(),
            )
            val lastChecked = state.lastChecked
            if (state.offline && lastChecked != null) {
                Text(
                    text = listLastCheckedText(lastChecked, state.lastCheckedAt),
                    style = AlterTheme.typography.labelXSmall,
                    color = AlterTheme.colors.textTertiary,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }

        // 받아 둔 충전소가 아예 없으면 헤더만 남기고 검색 · 필터 · 목록을 숨긴다 (Figma selection = empty, 807:59523).
        // 검색 · 필터 결과가 없을 때는 조건을 바꿀 수 있게 그대로 둔다
        if (state.showEmptyIllustration) {
            StationListEmpty(modifier = Modifier.weight(1f))
            return@Column
        }

        // 검색바 (REQ_SL_002 · REQ_SL_003)
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            value = state.query,
            onValueChange = onQueryChange,
            onMicClick = onMicClick,
            offline = state.offline,
        )

        // 필터 메뉴가 열려 있으면 목록을 어둡게 덮는다 (Figma 필터 드롭다운 scrim)
        var menuOpen by remember { mutableStateOf(false) }

        // 필터 영역 (정렬, 연결 타입, 속도)
        StationListFilterRow(
            state = state,
            onSortChange = onSortChange,
            onFilterChange = onFilterChange,
            onMenuOpenChange = { menuOpen = it },
        )

        // 안내 줄: 불러오는 중 · 빈 목록 · 오프라인 · 음성 안내 (REQ_CM_006 · REQ_SL_003)
        listNotice(state)?.let { notice ->
            Text(
                text = notice,
                fontSize = 15.sp,
                color = AlterTheme.colors.textTertiary,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        // 충전소 목록
        val listState = rememberLazyListState()
        // Figma scrollbar(디자이너 QA 01): 손잡이가 어두운 패널에서도 보이게 다크는 #F7F8FA
        val scrollbarTrack = AlterTheme.colors.textPrimary.copy(alpha = 0.08f)
        val scrollbarThumb = if (AlterTheme.colors.isDark) Color(0xFFF7F8FA) else AlterTheme.colors.gray300
        val scrimColor = AlterTheme.colors.screenBackground.copy(alpha = 0.92f)
        // 패널 맨 아래 색: 바탕 위에 bg_glow의 blue_100 55%
        val fadeColor = AlterTheme.colors.blue100.copy(alpha = 0.55f).compositeOver(AlterTheme.colors.containerBackgroundSubtle)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // Figma scrollbar: 패널 오른쪽 끝에 붙는 폭 5 막대 (바탕 basic_50 · 손잡이 basic_300). 넘칠 때만 그린다
                .drawWithContent {
                    drawContent()
                    drawStationListScrollbar(listState, track = scrollbarTrack, thumb = scrollbarThumb)
                    if (menuOpen) {
                        // 패널 안쪽 여백까지 덮는다
                        val pad = AlterMargin.Normal.toPx()
                        drawRect(color = scrimColor, topLeft = Offset(-pad, 0f), size = Size(size.width + pad * 2, size.height + pad))
                    }
                }
                // Figma fade_bottom: 아래에 더 있으면 패널 바닥색(배경 + blue_100 55%)을 덮어 마지막 항목이 흐려진다.
                // 끝까지 내리면 없앤다. 오프스크린 레이어로 내용을 지우는 방식은 다시 연결된 뒤 목록이 통째로
                // 안 그려지는 일이 있어 쓰지 않는다
                .drawWithContent {
                    drawContent()
                    if (listState.canScrollForward) {
                        val fadeHeight = LIST_FADE_HEIGHT.toPx().coerceAtMost(size.height)
                        drawRect(
                            brush = Brush.verticalGradient(
                                0f to fadeColor.copy(alpha = 0f),
                                1f to fadeColor,
                                startY = size.height - fadeHeight,
                                endY = size.height,
                            ),
                            topLeft = Offset(0f, size.height - fadeHeight),
                            size = Size(size.width, fadeHeight),
                        )
                    }
                },
            // Figma list gap 8
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.items) { station ->
                StationItem(
                    state = station,
                    modifier = Modifier.clickable { onStationClick(station) },
                    selected = station.stationId == selectedStationId,
                )
            }
        }
    }
}

@Composable
private fun StationListEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.img_empty_station_alert),
            contentDescription = null,
            modifier = Modifier.size(width = 260.dp, height = 188.dp),
        )
        // Figma CM_006 보드: 오프라인이어도 제목만 둔다. 연결 안내는 R1 빈 카드가 맡는다
        Text(
            text = stringResource(R.string.station_list_empty_title),
            style = AlterTheme.typography.bodyXSmall,
            color = AlterTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 목록 스크롤바. 목록 칸(패널 안쪽 여백 16 안)에서 오른쪽으로 비켜 패널 끝에 붙인다.
 * 항목 높이가 거의 같아 보이는 항목 평균으로 전체 길이를 어림한다.
 */
private fun DrawScope.drawStationListScrollbar(state: LazyListState, track: Color, thumb: Color) {
    if (!state.canScrollForward && !state.canScrollBackward) return
    val info = state.layoutInfo
    val visible = info.visibleItemsInfo
    if (visible.isEmpty() || info.totalItemsCount == 0) return
    val spacing = info.mainAxisItemSpacing.toFloat()
    val itemStep = visible.sumOf { it.size }.toFloat() / visible.size + spacing
    val contentHeight = itemStep * info.totalItemsCount - spacing
    val viewport = size.height
    val scrolled = state.firstVisibleItemIndex * itemStep + state.firstVisibleItemScrollOffset
    val width = SCROLLBAR_WIDTH.toPx()
    val x = size.width + AlterMargin.Normal.toPx() - width
    val radius = CornerRadius(SCROLLBAR_RADIUS.toPx(), SCROLLBAR_RADIUS.toPx())
    drawRoundRect(color = track, topLeft = Offset(x, 0f), size = Size(width, viewport), cornerRadius = radius)
    // 목록 길이에 비례하되 너무 짧아지지 않게 한다 (시안 손잡이는 목록 높이의 절반쯤)
    val minThumb = SCROLLBAR_MIN_THUMB.toPx().coerceAtMost(viewport * 0.6f)
    val thumbHeight = (viewport / contentHeight * viewport).coerceIn(minThumb, viewport)
    val progress = (scrolled / (contentHeight - viewport)).coerceIn(0f, 1f)
    drawRoundRect(
        color = thumb,
        topLeft = Offset(x, progress * (viewport - thumbHeight)),
        size = Size(width, thumbHeight),
        cornerRadius = radius,
    )
}

private val SCROLLBAR_WIDTH = 5.dp
private val SCROLLBAR_RADIUS = 4.dp
private val SCROLLBAR_MIN_THUMB = 160.dp

/**
 * R2 헤더의 마지막 확인 시각 (Figma CM_006 보드 ②): 1분 미만 "방금 기준", 하루 안 "14:30 기준", 그 전 "11.11 14:30 기준".
 * 카드(L1 · R1)는 "N분 전 기준"으로 짧게 쓰고, 목록 헤더는 시각으로 쓴다.
 */
@Composable
private fun listLastCheckedText(lastChecked: LastChecked, at: Instant?): String {
    if (lastChecked == LastChecked.JustNow || at == null) return lastCheckedText(lastChecked)
    val local = at.atZone(KoreaZone)
    val time = local.format(DateTimeFormatter.ofPattern("HH:mm"))
    // 경과 시간이 아니라 날짜로 나눈다: 어제 23:00 값을 오늘 09:00에 "23:00 기준"으로 쓰면 오늘 시각으로 읽힌다
    return if (local.toLocalDate() != LocalDate.now(KoreaZone)) {
        stringResource(R.string.station_list_last_checked_date_time, local.monthValue, local.dayOfMonth, time)
    } else {
        stringResource(R.string.station_list_last_checked_time, time)
    }
}

/** 목록 아래 흐림 높이 */
private val LIST_FADE_HEIGHT = 40.dp

@Composable
private fun listNotice(state: StationListUiState): String? = when {
    state.voice == VoiceSearchState.Listening -> stringResource(R.string.station_list_voice_listening)
    state.message == StationListMessage.SEARCH_UNAVAILABLE -> stringResource(R.string.station_list_offline_search_blocked)
    state.message == StationListMessage.VOICE_PERMISSION -> stringResource(R.string.station_list_voice_permission)
    state.message == StationListMessage.VOICE_UNAVAILABLE -> stringResource(R.string.station_list_voice_unavailable)
    state.message == StationListMessage.VOICE_ERROR -> stringResource(R.string.station_list_voice_error)
    state.isFilterEmpty -> stringResource(R.string.station_list_filter_empty)
    state.isLoading && state.items.isEmpty() -> stringResource(R.string.station_list_loading)
    // 검색 때문에 비었으면 반경 문구("3km 안에 없음") 대신 검색 결과 문구
    state.query.isNotBlank() && state.items.isEmpty() -> stringResource(R.string.station_list_search_empty)
    state.items.isEmpty() -> stringResource(R.string.station_list_empty)
    else -> null
}


private val previewState = StationListUiState(items = previewStations, visitSortEnabled = true)

@Preview(name = "RecommendedStationList(Light)", widthDp = 383, heightDp = 600)
@Composable
private fun RecommendedStationListLightPreview() {
    AlterChargeTheme(darkTheme = false) {
        RecommendedStationList(state = previewState)
    }
}

@Preview(name = "RecommendedStationList(Dark)", widthDp = 383, heightDp = 600)
@Composable
private fun RecommendedStationListDarkPreview() {
    AlterChargeTheme(darkTheme = true) {
        RecommendedStationList(state = previewState)
    }
}
