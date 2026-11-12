/** C 차량 상태 카드. mode별 배치는 CardVehicleStatus*Layout(s).kt에 있다 */
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

/** Figma card_vehicle_status 속성 mode */
enum class CardVehicleStatusMode {
    /** S-17 가로 790×1040 */
    LANDSCAPE,

    /** W-Extended 가로 770×1256 (366:39024) */
    LANDSCAPE_TALL,

    /** S-17 세로 776×652 */
    PORTRAIT,

    /** W-Extended 세로 736×816 (366:39032) */
    PORTRAIT_TALL,

    /** WF 가로 387×751 (366:39036) */
    WF_LANDSCAPE,

    /** WF 세로 584×666 (366:39040 · 452:54353, Q&A Q4로 580 → 584) */
    WF_PORTRAIT,

    /** W-Standard 세로 776×338 (598:57477 · 598:57512). 첫 화면에 L1 · R1이 들어오도록 타일을 뺀다 (Q&A Q3) */
    PORTRAIT_COMPACT,
}

/**
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

enum class VehicleImageKind {
    /** vehicle_charging */
    CHARGING,

    /** vehicle_driving */
    DRIVING,

    /** vehicle_top_view */
    TOP_VIEW,
}

/**
 * Figma card_vehicle_status (366:39011), 상황별 시안 (455:58776 01~12).
 * 칸을 채우므로 크기는 부모가 정해 줘야 하고, 스크롤 안에 크기 없이 넣으면 안 된다.
 *
 * @param mode 레이아웃 틀 안에서는 cardVehicleStatusModeFor(LocalAppLayout.current)로 고른다.
 * @param vehicleImage 바꿔 그리려면 넘긴 Modifier(자리 크기 포함)를 쓴다.
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
    // 다크 모드는 그림자 대신 면 색으로 높이를 나타낸다. S-17 가로만 한 겹이 더 있다
    val shadow = if (mode == CardVehicleStatusMode.LANDSCAPE) CardShadow else AlterShadows.Level1
    val shadowModifier = if (colors.isDark) Modifier else Modifier.alterShadow(shadow, CardRadius)

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .then(shadowModifier)
            .clip(shape)
            // card_vehicle_status/container/background → common/container/background (Q&A Q2)
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

/** card_vehicle_status/container/radius. 카드 변수라 AlterRadius에 없다 */
private val CardRadius = 32.dp

/** shadow/level_1 + 넓게 퍼지는 한 겹 */
private val CardShadow: List<ShadowLayer> =
    AlterShadows.Level1 + ShadowLayer(offsetY = 24.dp, blur = 64.dp, color = Color(0x12131F40))
