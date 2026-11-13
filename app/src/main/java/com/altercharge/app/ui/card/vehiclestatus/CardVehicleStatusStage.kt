/** 스테이지(차량 이미지 면)와 그 위에 상태 패널을 겹치는 틀 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import kotlin.math.roundToInt

/** 칩 안쪽 여백은 WF 16, 나머지 24 */
@Composable
internal fun Stage(
    chip: ConnectionChip,
    chipInset: Dp = AlterPadding.Large,
    background: Color = AlterTheme.colors.background,
    image: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(AlterRadius.XXLarge)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(background, shape),
    ) {
        image()
        ChipStatus(
            chip = chip,
            modifier = Modifier.padding(start = chipInset, top = chipInset),
        )
    }
}

/** 원은 스테이지보다 클 수 있어(예: 290.7 원 · 277 스테이지) 자리를 잡지 않고 뒤에 그린다 */
@Composable
internal fun StageVehicle(
    spec: StageImageSpec,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val backdropColor = AlterTheme.colors.surfaceHigh
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val diameter = spec.backdropSize?.toPx() ?: return@drawBehind
                val topLeft = spec.backdropAlignment.align(
                    IntSize(diameter.roundToInt(), diameter.roundToInt()),
                    IntSize(size.width.roundToInt(), size.height.roundToInt()),
                    layoutDirection,
                )
                drawCircle(
                    color = backdropColor,
                    radius = diameter / 2,
                    center = Offset(
                        x = topLeft.x + spec.backdropOffsetX.toPx() + diameter / 2,
                        y = topLeft.y + spec.backdropOffsetY.toPx() + diameter / 2,
                    ),
                )
            },
    ) {
        vehicleImage(
            VehicleImageKind.TOP_VIEW,
            Modifier
                .align(spec.imageAlignment)
                .padding(end = spec.imageEndPadding)
                .size(width = spec.imageWidth, height = spec.imageHeight),
        )
    }
}

/**
 * 위치는 패널이 겹친 부분까지 포함한 스테이지 기준이다.
 * 차 크기는 회전한 뒤의 크기다(Figma는 세로 이미지를 -90도 돌려 둔다).
 * @param backdropSize null이면 원이 없다
 */
internal class StageImageSpec(
    val backdropSize: Dp?,
    val imageWidth: Dp,
    val imageHeight: Dp,
    val backdropAlignment: Alignment = Alignment.Center,
    val backdropOffsetX: Dp = 0.dp,
    val backdropOffsetY: Dp = 0.dp,
    val imageAlignment: Alignment = Alignment.Center,
    val imageEndPadding: Dp = 0.dp,
)

/**
 * status_panel의 BACKGROUND_BLUR 40 대용. Compose에 뒤 흐림이 없어 스테이지를 한 번 더 그려 흐린 뒤
 * 패널이 겹치는 띠 모양으로 잘라낸다.
 * 흐림은 API 31 이상에서만 걸린다. minSdk 28~30에서는 흐리지 않은 복사본이 겹친다(크래시 없음, 차량은 API 34).
 *
 * - 스테이지 아래 끝도 흐려져야 경계가 사라져 [extendBelow]만큼 더 내려간다.
 * - 복사본을 사방 [PanelBackdropMargin]만큼 키워야 가장자리가 어두워지지 않는다.
 * - 부모보다 큰 상자는 Compose가 가운데로 밀어서, 높이를 "부모 + 여유 × 2"로 두고 복사본을 여유만큼 내려 원본과 맞춘다.
 */
@Composable
internal fun PanelBackdrop(
    bandHeight: Dp,
    extendBelow: Dp,
    stageBackground: Color = AlterTheme.colors.background,
    cardBackground: Color = AlterTheme.colors.containerBackground,
    image: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // 안쪽 Box 안에서는 BoxWithConstraints 값에 바로 닿을 수 없어(DslMarker) 여기서 미리 꺼낸다
        val stageWidth = maxWidth
        val stageHeight = maxHeight - extendBelow
        val backdropHeight = maxHeight
        // 위 두 모서리는 패널 모서리(24)와 같다
        val bandShape = remember(stageHeight, bandHeight, density) {
            GenericShape { size, _ ->
                val top = with(density) { (stageHeight - bandHeight).toPx() }.coerceAtLeast(0f)
                val radius = with(density) { AlterRadius.XXLarge.toPx() }
                addRoundRect(
                    RoundRect(
                        left = 0f,
                        top = top,
                        right = size.width,
                        bottom = size.height,
                        topLeftCornerRadius = CornerRadius(radius),
                        topRightCornerRadius = CornerRadius(radius),
                    ),
                )
            }
        }
        // 복사본은 장식이라 접근성 트리에서 뺀다 (안 그러면 차량 이미지 설명이 두 번 읽힌다)
        Box(Modifier.matchParentSize().clip(bandShape).clearAndSetSemantics {}) {
            Box(
                modifier = Modifier
                    .requiredSize(
                        width = stageWidth + PanelBackdropMargin * 2,
                        height = backdropHeight + PanelBackdropMargin * 2,
                    )
                    .background(cardBackground)
                    .blur(PanelBackdropBlurRadius, BlurredEdgeTreatment.Unbounded),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = PanelBackdropMargin)
                        .size(width = stageWidth, height = stageHeight)
                        .background(stageBackground),
                ) {
                    image()
                }
            }
        }
    }
}

/**
 * BACKGROUND_BLUR 40 = 가우스 표준편차 20. Modifier.blur의 radius는 표준편차로 쓰인다.
 * (BlurMaskFilter용 반지름 변환을 쓰면 시안보다 넓게 번진다 — 에뮬레이터 캡처로 확인)
 */
internal val PanelBackdropBlurRadius = 20.dp

/** 표준편차 20의 3배 */
private val PanelBackdropMargin = 60.dp

/** 스테이지 · 흐림 복사본 · 패널 순으로 쌓는다. 패널 높이를 먼저 재고 스테이지는 남은 높이 + [overlap]을 받는다 */
@Composable
internal fun StageWithPanel(
    overlap: Dp,
    chip: ConnectionChip,
    modifier: Modifier,
    chipInset: Dp = AlterPadding.Large,
    stageBackground: Color = AlterTheme.colors.background,
    image: @Composable () -> Unit,
    panel: @Composable () -> Unit,
) {
    val stage: @Composable () -> Unit = {
        Stage(chip = chip, chipInset = chipInset, background = stageBackground, image = image)
    }
    val backdrop: @Composable () -> Unit = {
        PanelBackdrop(
            bandHeight = overlap,
            extendBelow = PanelBackdropExtendBelow,
            stageBackground = stageBackground,
            image = image,
        )
    }
    Layout(contents = listOf(stage, backdrop, panel), modifier = modifier) { measurables, constraints ->
        val (stageMeasurables, backdropMeasurables, panelMeasurables) = measurables
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val panelPlaceable = panelMeasurables.first()
            .measure(Constraints(minWidth = width, maxWidth = width, maxHeight = height))
        val stageHeight = (height - panelPlaceable.height + overlap.roundToPx()).coerceIn(0, height)
        val stagePlaceable = stageMeasurables.first().measure(Constraints.fixed(width, stageHeight))
        val backdropHeight = (stageHeight + PanelBackdropExtendBelow.roundToPx()).coerceAtMost(height)
        val backdropPlaceable = backdropMeasurables.first().measure(Constraints.fixed(width, backdropHeight))
        layout(width, height) {
            stagePlaceable.place(0, 0)
            backdropPlaceable.place(0, 0)
            panelPlaceable.place(0, height - panelPlaceable.height)
        }
    }
}

/** 표준편차(20)의 2배면 경계가 사라진다 */
private val PanelBackdropExtendBelow = PanelBackdropBlurRadius * 2
