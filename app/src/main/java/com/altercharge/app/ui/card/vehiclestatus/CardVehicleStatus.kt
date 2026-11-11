/**
 * C 차량 상태 카드의 공개 진입점: CardVehicleStatus, mode(CardVehicleStatusMode)와 고르는 함수, 차량 이미지 종류.
 * mode별 배치는 CardVehicleStatus*Layout(s).kt, 조각은 Headline · Stage · Battery · Chip · Tile · VehicleImage 파일에 있다.
 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterShadows
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.ShadowLayer
import com.altercharge.app.ui.designsystem.theme.alterShadow

/**
 * 카드 배치. Figma card_vehicle_status 속성 mode.
 * - LANDSCAPE: S-17 가로 790×1040 (세로로 긴 카드)
 * - LANDSCAPE_TALL: W-Extended 가로 770×1256 (스펙 시트 366:39024)
 * - PORTRAIT: S-17 세로 776×652 (가로로 넓은 카드)
 * - PORTRAIT_TALL: W-Extended 세로 736×816 (366:39032)
 * - WF_LANDSCAPE: WF 가로 387×751, 상태 블록을 세로로 쌓는다 (366:39036)
 * - WF_PORTRAIT: WF 세로 584×666 (366:39040, 화면 인스턴스 452:54353. Q&A Q4로 580 → 584, 안쪽은 모두 폭 채움)
 * - PORTRAIT_COMPACT: W-Standard 세로 776×338 (598:57477 · 598:57512). 첫 화면에 L1 · R1이 들어오도록
 *   왼쪽에 헤드라인 + 배터리, 오른쪽에 차량 stage 300을 두고 정보 타일은 뺀다 (개발자 Q&A Q3)
 */
enum class CardVehicleStatusMode {
    LANDSCAPE,
    LANDSCAPE_TALL,
    PORTRAIT,
    PORTRAIT_TALL,
    WF_LANDSCAPE,
    WF_PORTRAIT,
    PORTRAIT_COMPACT,
}

/**
 * 레이아웃 정보로 mode를 고른다. W-Standard 세로는 PORTRAIT_COMPACT.
 * TODO 디자이너 확인: W-Standard 가로(높이 810)는 시안이 없어 S-17 가로 모드로 대신한다.
 */
fun cardVehicleStatusModeFor(info: AppLayoutInfo): CardVehicleStatusMode = when (info.formFactor) {
    FormFactor.WF ->
        if (info.isLandscape) CardVehicleStatusMode.WF_LANDSCAPE else CardVehicleStatusMode.WF_PORTRAIT
    FormFactor.W_EXTENDED ->
        if (info.isLandscape) CardVehicleStatusMode.LANDSCAPE_TALL else CardVehicleStatusMode.PORTRAIT_TALL
    FormFactor.W_STANDARD ->
        if (info.isLandscape) CardVehicleStatusMode.LANDSCAPE else CardVehicleStatusMode.PORTRAIT_COMPACT
    FormFactor.S17 ->
        if (info.isLandscape) CardVehicleStatusMode.LANDSCAPE else CardVehicleStatusMode.PORTRAIT
}

/** 차량 이미지 종류. 상황에 따라 카드가 고른다. */
enum class VehicleImageKind {
    /** 가로 카드 · 충전기에 꽂힌 측면 (vehicle_charging) */
    CHARGING,

    /** 가로 카드 · 도로 위 측면 (vehicle_driving) */
    DRIVING,

    /** 세로 카드 · 위에서 본 차 (vehicle_top_view) */
    TOP_VIEW,
}

/**
 * C 차량 상태 카드. Figma card_vehicle_status (366:39011), 상황별 시안 (455:58776 01~12).
 * 보기 전용 카드다(눌러서 하는 동작이 없다). 화면 읽기에서는 카드 전체를 한 덩어리로 읽는다.
 * 카드가 주어진 칸을 채운다(fillMaxSize). 크기는 부모(레이아웃 슬롯)가 정해 줘야 하고, 스크롤 안에 크기 없이 넣으면 안 된다.
 *
 * @param mode 레이아웃 틀 안에서는 cardVehicleStatusModeFor(LocalAppLayout.current)로 고른다.
 * @param vehicleImage 차량 이미지 자리. 기본은 종류별 에셋([VehicleImage]). 바꿔 그리려면 넘긴 Modifier(자리 크기 포함)를 쓴다.
 */
@Composable
fun CardVehicleStatus(
    state: CardVehicleStatusUiState,
    modifier: Modifier = Modifier,
    mode: CardVehicleStatusMode = CardVehicleStatusMode.LANDSCAPE,
    vehicleImage: @Composable (kind: VehicleImageKind, modifier: Modifier) -> Unit = { kind, imageModifier ->
        VehicleImage(kind, imageModifier)
    },
) {
    val colors = AlterTheme.colors
    val content = buildContent(state, mode)
    val shape = RoundedCornerShape(CardRadius)
    // 다크 모드는 그림자 대신 면 색으로 높이를 나타낸다 (AlterShadows 설명).
    // S-17 가로만 0 24 64 한 겹이 더 있고, 나머지 모드는 shadow/level_1 한 겹뿐이다
    val shadow = if (mode == CardVehicleStatusMode.LANDSCAPE) CardShadow else AlterShadows.Level1
    val shadowModifier = if (colors.isDark) Modifier else Modifier.alterShadow(shadow, CardRadius)

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .then(shadowModifier)
            .clip(shape)
            // card_vehicle_status/container/background → common/container/background (다크 #1D1E21, 개발자 Q&A Q2)
            .background(colors.containerBackground, shape)
            .padding(AlterMargin.XSmall),
    ) {
        when (mode) {
            CardVehicleStatusMode.LANDSCAPE -> LandscapeLayout(state, content, vehicleImage)
            CardVehicleStatusMode.PORTRAIT -> PortraitLayout(state, content, vehicleImage)
            CardVehicleStatusMode.LANDSCAPE_TALL -> TallLayout(state, content, vehicleImage, LandscapeTall)
            CardVehicleStatusMode.PORTRAIT_TALL -> TallLayout(state, content, vehicleImage, PortraitTall)
            CardVehicleStatusMode.WF_LANDSCAPE -> WfLandscapeLayout(state, content, vehicleImage)
            CardVehicleStatusMode.WF_PORTRAIT -> WfPortraitLayout(state, content, vehicleImage)
            CardVehicleStatusMode.PORTRAIT_COMPACT -> CompactLayout(state, content, vehicleImage)
        }
    }
}

/**
 * card_vehicle_status/container/radius. 카드 변수라 AlterRadius(02 Dimensions, 최대 xxlarge 24)에 없다.
 * L1의 card_info/container/radius도 32지만 카드별 변수라 각자 둔다.
 */
private val CardRadius = 32.dp

/** 카드 그림자: shadow/level_1 + 넓게 퍼지는 한 겹 (0 24 64, #131F40 7%) */
private val CardShadow: List<ShadowLayer> =
    AlterShadows.Level1 + ShadowLayer(offsetY = 24.dp, blur = 64.dp, color = Color(0x12131F40))
