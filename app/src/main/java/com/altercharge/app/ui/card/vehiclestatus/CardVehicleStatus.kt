package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterLetterSpacing
import com.altercharge.app.ui.designsystem.theme.AlterLineHeightStyle
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterShadows
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.AstaSans
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

// ---------------------------------------------------------------------------
// 배치
// ---------------------------------------------------------------------------

@Composable
private fun LandscapeLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
    ) {
        Headline(
            text = content.headline,
            maxLines = LANDSCAPE_HEADLINE_MAX_LINES,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlterPadding.XLarge, vertical = AlterPadding.Large),
        )
        StageWithPanel(
            overlap = LandscapePanelOverlap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            stage = {
                Stage(chip = state.chip) {
                    vehicleImage(content.landscapeImage, Modifier.fillMaxSize())
                }
            },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.XLarge,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
                        Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.card_vehicle_status_battery),
                                    style = typography.labelSmall,
                                    color = colors.textSecondary,
                                )
                                BatteryGauge(level = content.gaugeLevel, fill = content.accent)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Soc(
                                    state = state,
                                    content = content,
                                    valueStyle = typography.numericHero,
                                    unitColor = colors.textTertiary,
                                    glow = true,
                                )
                                StatusText(
                                    status = content.status,
                                    numberStyle = typography.numericMedium.copy(color = colors.textPrimary),
                                    unitStyle = typography.labelMedium.copy(color = colors.textTertiary),
                                    plainStyle = typography.labelMedium.copy(color = colors.textTertiary),
                                    modifier = Modifier.alignByBaseline(),
                                )
                            }
                        }
                        // 타일이 하나뿐이면(09 배터리 위험 · 미연결) 값은 왼쪽 반에 두고 구분선은 패널 폭 전체에 긋는다 (시안).
                        val singleTile = content.tiles.size == 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (singleTile) Modifier.tileDivider(colors.surfaceAccent) else Modifier),
                            horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large),
                        ) {
                            content.tiles.forEach { tile ->
                                InfoTile(
                                    tile = tile,
                                    valueStyle = landscapeTileValueStyle(typography.numericMedium),
                                    unitStyle = LandscapeTileUnitStyle,
                                    drawDivider = !singleTile,
                                )
                            }
                            if (singleTile) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun PortraitLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AlterPadding.Large, start = WideSidePadding, end = WideSidePadding),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 2줄을 넘으면 60 → 56 → 48 → 40으로 줄인다 (개발자 Q&A Q1)
            AutoShrinkHeadline(
                text = content.portraitHeadline,
                styles = portraitHeadlineStyles(),
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
            ) {
                content.portraitTiles.forEach { tile ->
                    InfoTile(
                        tile = tile,
                        valueStyle = typography.numericSmall,
                        unitStyle = typography.labelSmall,
                    )
                }
            }
        }
        StageWithPanel(
            overlap = PortraitPanelOverlap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            stage = {
                // Figma 세로 stage는 variant가 둘이다(상황 보드 455:58776 세로 01~12).
                // 둘 다 패널이 겹친 부분까지 포함한 스테이지 기준이고, 차 자리는 세로 가운데 · 오른쪽 여백 24.
                Stage(chip = state.chip) {
                    StageVehicle(
                        spec = if (content.isCharging) PortraitStageCharging else PortraitStageUnplugged,
                        vehicleImage = vehicleImage,
                    )
                }
            },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.Large,
                    glassAlpha = FlatStageGlassAlpha,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        // 세로 시안(455:58776 세로 01~12)은 SOC 숫자 빛번짐이 있다
                        BatteryValue(
                            state = state,
                            content = content,
                            labelStyle = typography.labelMedium,
                            socStyle = portraitSocStyle(typography.numericHero),
                            glow = true,
                        )
                        BatteryDetail(
                            content = content,
                            statusStyle = typography.titleSmall,
                            horizontalAlignment = Alignment.End,
                        )
                    }
                }
            },
        )
    }
}

/** W-Extended 가로 · 세로. 헤드라인 / 스테이지 + 배터리 패널 / 정보 타일 순서로 쌓는다. */
@Composable
private fun TallLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
    spec: TallSpec,
) {
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        val headlineModifier = Modifier
            .fillMaxWidth()
            .padding(top = AlterPadding.Large, start = WideSidePadding, end = WideSidePadding)
        if (spec.autoShrinkHeadline) {
            AutoShrinkHeadline(
                text = content.headline,
                styles = portraitHeadlineStyles(),
                modifier = headlineModifier,
            )
        } else {
            Headline(
                text = content.headline,
                maxLines = LANDSCAPE_HEADLINE_MAX_LINES,
                modifier = headlineModifier,
            )
        }
        StageWithPanel(
            overlap = spec.panelOverlap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            stage = {
                // TODO 디자이너 확인(변수 연결): landscape_tall 충전 중(366:37686) stage만 배경이
                //   panel_station_list/item/enabled/container(흰색)에 연결돼 있다. 다른 variant는 stage/background(#F7F8FA).
                //   화면에 보이는 값(흰색)을 따랐다.
                val stageBackground = if (content.isCharging && spec.chargingStageOnSurface) {
                    AlterTheme.colors.surface
                } else {
                    AlterTheme.colors.background
                }
                Stage(chip = state.chip, background = stageBackground) {
                    StageVehicle(
                        spec = if (content.isCharging) spec.stageCharging else spec.stageUnplugged,
                        vehicleImage = vehicleImage,
                    )
                }
            },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.Large,
                    glassAlpha = FlatStageGlassAlpha,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        BatteryValue(
                            state = state,
                            content = content,
                            labelStyle = typography.labelMedium,
                            socStyle = spec.socStyle(typography.numericHero),
                        )
                        BatteryDetail(
                            content = content,
                            statusStyle = typography.titleSmall,
                            horizontalAlignment = Alignment.End,
                        )
                    }
                }
            },
        )
        // 타일이 하나면 폭 전체를 쓴다 (unplugged variant의 tile_range 674/640)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WideSidePadding),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
        ) {
            content.tiles.forEach { tile ->
                InfoTile(
                    tile = tile,
                    valueStyle = typography.numericSmall,
                    unitStyle = typography.labelSmall,
                )
            }
        }
    }
}

/** WF 가로 (폭 387). 배터리 패널 안에 값 / 게이지 · 남은 시간 / 요약 한 줄을 세로로 쌓는다. */
@Composable
private fun WfLandscapeLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Headline(
            text = content.headline,
            maxLines = WF_LANDSCAPE_HEADLINE_MAX_LINES,
            style = typography.headlineSmall,
            lineBreak = PortraitHeadlineLineBreak,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AlterPadding.Large, start = AlterPadding.Large, end = AlterPadding.Large),
        )
        StageWithPanel(
            overlap = PortraitPanelOverlap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            stage = {
                Stage(chip = state.chip, chipInset = AlterPadding.Normal) {
                    StageVehicle(spec = WfLandscapeStage, vehicleImage = vehicleImage)
                }
            },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.Large,
                    glassAlpha = FlatStageGlassAlpha,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(BatteryTileGap)) {
                        BatteryValue(
                            state = state,
                            content = content,
                            labelStyle = typography.labelSmall,
                            socStyle = portraitSocStyle(typography.numericHero),
                        )
                        BatteryDetail(
                            content = content,
                            statusStyle = typography.bodyLarge,
                            horizontalAlignment = Alignment.Start,
                        )
                        InfoSummary(content.summary)
                    }
                }
            },
        )
    }
}

/** WF 세로 (584). 배터리 값 · 게이지 줄 아래에 요약 한 줄. */
@Composable
private fun WfPortraitLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Headline(
            text = content.headline,
            maxLines = WF_PORTRAIT_HEADLINE_MAX_LINES,
            style = typography.headlineMedium,
            lineBreak = PortraitHeadlineLineBreak,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AlterPadding.Large, start = AlterPadding.Large, end = AlterPadding.Large),
        )
        StageWithPanel(
            overlap = PortraitPanelOverlap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            stage = {
                Stage(chip = state.chip, chipInset = AlterPadding.Normal) {
                    StageVehicle(spec = WfPortraitStage, vehicleImage = vehicleImage)
                }
            },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.Large,
                    glassAlpha = FlatStageGlassAlpha,
                ) {
                    // Figma 노드 위치상 요약은 battery_row 바로 아래(y 178)에 붙는다 (battery_tile 높이 238 고정)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            BatteryValue(
                                state = state,
                                content = content,
                                labelStyle = typography.labelSmall,
                                socStyle = portraitSocStyle(typography.numericHero),
                            )
                            BatteryDetail(
                                content = content,
                                statusStyle = typography.bodyLarge,
                                horizontalAlignment = Alignment.End,
                            )
                        }
                        InfoSummary(content.summary)
                    }
                }
            },
        )
    }
}

/**
 * W-Standard 세로 776×338 (598:57477 충전 중 · 598:57512 미연결).
 * 왼쪽 status_block(헤드라인 + 배터리 타일), 오른쪽 stage 300. 정보 타일은 없다.
 */
@Composable
private fun CompactLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val typography = AlterTheme.typography
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(AlterMargin.Small),
        ) {
            // 48 → 최소 40, 2줄 말줄임
            AutoShrinkHeadline(
                text = content.portraitHeadline,
                styles = listOf(typography.headlineSmall, typography.titleLarge),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AlterPadding.Large, end = AlterPadding.XSmall, top = AlterMargin.Small),
            )
            GlassPanel(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = AlterPadding.Medium,
                verticalPadding = AlterPadding.Normal,
                glassAlpha = FlatStageGlassAlpha,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    BatteryValue(
                        state = state,
                        content = content,
                        labelStyle = typography.labelMedium,
                        socStyle = compactSocStyle(typography.numericHero),
                    )
                    // 미연결 variant(598:57512)는 range 문구를 숨기고 게이지만 SOC 줄 아래에 맞춘다
                    BatteryDetail(
                        content = content,
                        statusStyle = typography.titleSmall,
                        horizontalAlignment = Alignment.End,
                        gap = AlterMargin.XSmall,
                        showStatus = state.charging != ChargingState.UNPLUGGED,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .width(CompactStageWidth)
                .fillMaxHeight(),
        ) {
            Stage(chip = state.chip) {
                StageVehicle(spec = CompactStage, vehicleImage = vehicleImage)
            }
        }
    }
}

/** 스테이지 아래쪽에 상태 패널을 겹쳐 올린다. 패널 높이를 먼저 재고 나머지를 스테이지에 준다. */
@Composable
private fun StageWithPanel(
    overlap: Dp,
    modifier: Modifier,
    stage: @Composable () -> Unit,
    panel: @Composable () -> Unit,
) {
    Layout(contents = listOf(stage, panel), modifier = modifier) { measurables, constraints ->
        val (stageMeasurables, panelMeasurables) = measurables
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val panelPlaceable = panelMeasurables.first()
            .measure(Constraints(minWidth = width, maxWidth = width, maxHeight = height))
        val stageHeight = (height - panelPlaceable.height + overlap.roundToPx()).coerceIn(0, height)
        val stagePlaceable = stageMeasurables.first().measure(Constraints.fixed(width, stageHeight))
        layout(width, height) {
            stagePlaceable.place(0, 0)
            panelPlaceable.place(0, height - panelPlaceable.height)
        }
    }
}

// ---------------------------------------------------------------------------
// mode별 치수 (Figma card_vehicle_status 변수). 디자인 값에 없는 것만 둔다.
// ---------------------------------------------------------------------------

/** 헤드라인 최대 줄 수. 넘치면 말줄임 */
private const val LANDSCAPE_HEADLINE_MAX_LINES = 2

/**
 * 세로 헤드라인 자동 축소 단계 (portrait · portrait_tall).
 * card_vehicle_status/headline/font_size_max 60(headline/large) → 56(headline/medium) → 48(headline/small)
 * → font_size_min 40(title/large)
 */
@Composable
private fun portraitHeadlineStyles(): List<TextStyle> {
    val typography = AlterTheme.typography
    return listOf(typography.headlineLarge, typography.headlineMedium, typography.headlineSmall, typography.titleLarge)
}

/** WF 가로 헤드라인 칸은 높이가 내용을 따른다(시안 2줄). 긴 문구는 3줄까지 */
private const val WF_LANDSCAPE_HEADLINE_MAX_LINES = 3

/** WF 세로 헤드라인 칸은 높이 192 고정(2줄) */
private const val WF_PORTRAIT_HEADLINE_MAX_LINES = 2

/** 세로 헤드라인: 단어(한국어는 어절) 단위 줄바꿈 */
internal val PortraitHeadlineLineBreak = LineBreak(
    strategy = LineBreak.Strategy.HighQuality,
    strictness = LineBreak.Strictness.Normal,
    wordBreak = LineBreak.WordBreak.Phrase,
)

/**
 * card_vehicle_status/container/radius. 카드 변수라 AlterRadius(02 Dimensions, 최대 xxlarge 24)에 없다.
 * L1의 card_info/container/radius도 32지만 카드별 변수라 각자 둔다.
 */
private val CardRadius = 32.dp

/** 카드 그림자: shadow/level_1 + 넓게 퍼지는 한 겹 (0 24 64, #131F40 7%) */
private val CardShadow: List<ShadowLayer> =
    AlterShadows.Level1 + ShadowLayer(offsetY = 24.dp, blur = 64.dp, color = Color(0x12131F40))

/** 상태 패널이 스테이지 위로 겹치는 높이 */
private val LandscapePanelOverlap = 56.dp
private val PortraitPanelOverlap = 24.dp

/** 세로 헤드라인 줄 · tall 헤드라인 · tall 타일 줄 좌우 여백 */
private val WideSidePadding = 40.dp

/** 세로 충전기 쪽: 원 253, 세로 가운데에서 10.5 위 · 오른쪽 여백 19 (backdrop top = 50% - 10.5). 차 242×153 세로 가운데 */
private val PortraitStageCharging = StageImageSpec(
    backdropSize = 253.dp,
    backdropAlignment = Alignment.CenterEnd,
    backdropOffsetX = (-19).dp,
    backdropOffsetY = (-10.5).dp,
    imageWidth = 242.dp,
    imageHeight = 153.dp,
    imageAlignment = Alignment.CenterEnd,
    imageEndPadding = 24.dp,
)

/** 세로 미연결 · 오프라인 (01 · 02 · 09 · 12): 원 217.6, 세로 가운데 · 오른쪽 밖으로 15.17. 차 139×115(115×139를 -90도) */
private val PortraitStageUnplugged = StageImageSpec(
    backdropSize = 217.6.dp,
    backdropAlignment = Alignment.CenterEnd,
    backdropOffsetX = 15.17.dp,
    imageWidth = 139.dp,
    imageHeight = 115.dp,
    imageAlignment = Alignment.CenterEnd,
    imageEndPadding = 24.dp,
)

/** W-Extended 모드별 값. Figma card_vehicle_status mode=landscape_tall (366:37686 · 366:37704) / portrait_tall (366:37763 · 366:37782) */
private class TallSpec(
    val panelOverlap: Dp,
    /** 헤드라인을 2줄 안에 맞춰 자동 축소할지 (portrait_tall만, 개발자 Q&A Q1) */
    val autoShrinkHeadline: Boolean,
    /** 충전기 쪽 스테이지 배경을 카드 면(흰색)으로 둘지 */
    val chargingStageOnSurface: Boolean,
    val stageCharging: StageImageSpec,
    val stageUnplugged: StageImageSpec,
    val socStyle: (TextStyle) -> TextStyle,
)

/**
 * W-Ext 가로 770×1256: 패널 겹침 56, SOC 112(numericHero 그대로).
 * 충전기 쪽은 원 없이 차 626×441 가운데, 미연결은 원 529.22 가운데 · 차 391×473.
 */
private val LandscapeTall = TallSpec(
    panelOverlap = LandscapePanelOverlap,
    autoShrinkHeadline = false,
    chargingStageOnSurface = true,
    stageCharging = StageImageSpec(backdropSize = null, imageWidth = 626.dp, imageHeight = 441.dp),
    stageUnplugged = StageImageSpec(backdropSize = 529.22.dp, imageWidth = 391.dp, imageHeight = 473.dp),
    socStyle = { it },
)

/** W-Ext 세로 736×816: 패널 겹침 24, SOC 80. 원 290.7 가운데, 차 282×233(충전기 쪽) · 186×154(미연결), 둘 다 -90도 */
private val PortraitTall = TallSpec(
    panelOverlap = PortraitPanelOverlap,
    autoShrinkHeadline = true,
    chargingStageOnSurface = false,
    stageCharging = StageImageSpec(backdropSize = 290.7.dp, imageWidth = 282.dp, imageHeight = 233.dp),
    stageUnplugged = StageImageSpec(backdropSize = 290.7.dp, imageWidth = 186.dp, imageHeight = 154.dp),
    socStyle = ::portraitSocStyle,
)

/** WF 가로 (366:37801 · 366:37818, 두 variant 같음): 원 226.1 세로 가운데 · 오른쪽 밖으로 8.3, 차 162×134(-90도) 오른쪽 여백 24 */
private val WfLandscapeStage = StageImageSpec(
    backdropSize = 226.1.dp,
    backdropAlignment = Alignment.CenterEnd,
    backdropOffsetX = 8.3.dp,
    imageWidth = 162.dp,
    imageHeight = 134.dp,
    imageAlignment = Alignment.CenterEnd,
    imageEndPadding = 24.dp,
)

/** WF 세로 (366:37835 · 366:37853, 두 variant 같음): 원 265.2 가운데, 차 170×140(-90도) 가운데 */
private val WfPortraitStage = StageImageSpec(backdropSize = 265.2.dp, imageWidth = 170.dp, imageHeight = 140.dp)

/** 가로 타일 단위 글자 18/27 (디자인 값에 없는 크기). 자간 · 줄 높이 처리는 디자인 값 글자와 같다 */
private val LandscapeTileUnitStyle = TextStyle(
    fontFamily = AstaSans,
    fontWeight = AlterFontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 27.sp,
    letterSpacing = AlterLetterSpacing,
    lineHeightStyle = AlterLineHeightStyle,
)

/** 가로 타일 값: numeric/medium 40에 줄 높이만 1.2배(48). Figma tile_range · tile_* 값 */
private fun landscapeTileValueStyle(base: TextStyle): TextStyle = base.copy(lineHeight = 48.sp)

/** portrait_compact stage 폭 */
private val CompactStageWidth = 300.dp

/** portrait_compact (두 variant 같음): 원 240 세로 가운데 · 오른쪽 여백 30, 차 242×153(-90도) 가운데 */
private val CompactStage = StageImageSpec(
    backdropSize = 240.dp,
    backdropAlignment = Alignment.CenterEnd,
    backdropOffsetX = (-30).dp,
    imageWidth = 242.dp,
    imageHeight = 153.dp,
)

/** portrait_compact SOC 숫자 56 (디자인 값에 없는 크기, 줄 높이 1.5배 · 굵기 · 자간은 numericHero와 같다) */
private fun compactSocStyle(base: TextStyle): TextStyle = base.copy(fontSize = 56.sp, lineHeight = 84.sp)

/** 세로 SOC 숫자 80 (디자인 값에 없는 크기, 굵기 · 자간은 numericHero와 같다) */
private fun portraitSocStyle(base: TextStyle): TextStyle = base.copy(fontSize = 80.sp, lineHeight = 120.sp)
