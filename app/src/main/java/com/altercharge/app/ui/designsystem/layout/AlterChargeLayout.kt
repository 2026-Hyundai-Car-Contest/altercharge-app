package com.altercharge.app.ui.designsystem.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * 홈 화면 레이아웃 틀. 슬롯 6개를 폼팩터와 방향에 맞게 배치만 하고, 배경은 화면 쪽에서 칠한다.
 *
 * - 가로: 앱 바 자리(80) → L · C · R, 스크롤 없음 (Layout Rules · 스크롤).
 *   S-17 높이 합: 32 + 80(상태 바 56 + 빈칸 24) + 1040 + 32 = 1184. 본문 시작 y 112 (body 455:58813).
 * - 세로: 상태 바 → C → L1 | R1 → L2 | R2, 세로 스크롤 (455:58776). 부모가 스크롤이라 슬롯 높이는 틀이 정한다.
 *
 * @param leftColumnScrollable 가로 L2 펼침(상황 15)에서 왼쪽 열만 스크롤한다.
 *   이때 l1 · l2는 높이 제약이 무한대라 안에서 fillMaxHeight · weight를 쓰면 안 된다. WF는 무시한다.
 */
@Composable
fun AlterChargeLayout(
    statusBar: @Composable () -> Unit,
    l1: @Composable () -> Unit,
    l2: @Composable () -> Unit,
    c: @Composable () -> Unit,
    r1: @Composable () -> Unit,
    r2: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leftColumnScrollable: Boolean = false,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val isLandscape = maxWidth >= maxHeight
        val formFactor = FormFactor.closestTo(maxWidth, maxHeight)
        val spec = formFactor.spec(isLandscape)
        val layoutInfo = remember(formFactor, isLandscape) { AppLayoutInfo(formFactor, isLandscape) }
        CompositionLocalProvider(LocalAppLayout provides layoutInfo) {
            if (isLandscape) {
                LandscapeLayout(
                    spec, formFactor.landscapeSpans, maxWidth,
                    leftColumnScrollable && formFactor.landscapeLeftScrollable,
                    statusBar, l1, l2, c, r1, r2,
                )
            } else {
                PortraitLayout(PortraitSpec.of(formFactor, spec), statusBar, l1, l2, c, r1, r2)
            }
        }
    }
}

private val StatusBarSlotHeight = 56.dp

/** Layout Rules · 상단 영역 */
private val LandscapeAppBarArea = 80.dp

/** 상황 15 (461:16766): 왼쪽 열보다 좌우로 8씩 넓다 */
private val LandscapeFadeHeight = 160.dp
private val LandscapeFadeOutset = AlterMargin.XSmall

@Composable
private fun LandscapeLayout(
    spec: AreaSpec,
    spans: ColumnSpans,
    areaWidth: Dp,
    leftColumnScrollable: Boolean,
    statusBar: @Composable () -> Unit,
    l1: @Composable () -> Unit,
    l2: @Composable () -> Unit,
    c: @Composable () -> Unit,
    r1: @Composable () -> Unit,
    r2: @Composable () -> Unit,
) {
    // S-17 가로: L 383 · C 790 · R 383
    val contentWidth = areaWidth - spec.margin * 2
    val columnWidth = (contentWidth - spec.gutter * (spec.columns - 1)) / spec.columns
    fun spanWidth(columns: Int): Dp = columnWidth * columns + spec.gutter * (columns - 1)
    val leftScrollState = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spec.margin, vertical = spec.vertical),
        ) {
            Box(Modifier.fillMaxWidth().height(LandscapeAppBarArea)) {
                Box(Modifier.fillMaxWidth().height(StatusBarSlotHeight)) { statusBar() }
            }
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spec.gutter),
            ) {
                val leftModifier = Modifier.width(spanWidth(spans.left)).fillMaxHeight()
                if (leftColumnScrollable) {
                    Column(
                        modifier = leftModifier.verticalScroll(leftScrollState),
                        verticalArrangement = Arrangement.spacedBy(spec.gutter),
                    ) {
                        l1()
                        l2()
                    }
                } else {
                    Column(leftModifier, verticalArrangement = Arrangement.spacedBy(spec.gutter)) {
                        l1()
                        FillRest { l2() }
                    }
                }
                Box(Modifier.width(spanWidth(spans.center)).fillMaxHeight()) { c() }
                Column(
                    modifier = Modifier.width(spanWidth(spans.right)).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spec.gutter),
                ) {
                    r1()
                    FillRest { r2() }
                }
            }
        }
        // 끝까지 내리면 마지막 내용이 가려지지 않게 숨긴다. derivedStateOf로 끝 도달 여부가 바뀔 때만 다시 그린다.
        val showLeftFade by remember(leftScrollState) { derivedStateOf { leftScrollState.canScrollForward } }
        if (leftColumnScrollable && showLeftFade) {
            ScrollFade(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = spec.margin - LandscapeFadeOutset)
                    .width(spanWidth(spans.left) + LandscapeFadeOutset * 2)
                    .height(LandscapeFadeHeight),
            )
        }
    }
}

@Composable
private fun ColumnScope.FillRest(content: @Composable () -> Unit) {
    Box(Modifier.weight(1f).fillMaxWidth()) { content() }
}

/** 하이파이 scroll_fade. 터치를 받지 않아 아래 카드 조작을 막지 않는다 */
@Composable
private fun ScrollFade(modifier: Modifier) {
    // 다크 페이드는 시안에 없어 화면 배경 다크 값을 쓴다
    val color = AlterTheme.colors.screenBackground
    Box(
        modifier.background(
            Brush.verticalGradient(
                0f to color.copy(alpha = 0f),
                0.55f to color.copy(alpha = 0.78f),
                1f to color.copy(alpha = 0.96f),
            ),
        ),
    )
}

/**
 * 세로 배치 값. 규칙 표(pleos Layout)와 하이파이 화면이 다르면 하이파이 화면 값을 따른다.
 * 첫 화면에 L1과 R1이 보여야 한다 (Layout Rules · 첫 화면 행동).
 *
 * - S-17 · W-Extended: S-17 세로 하이파이 (461:14853). 상태 바 y 32 → C y 108 → 1행 y 780 → 2행 y 1168.
 * - W-Standard: 452:53649 (800×810). layout 변수가 연결돼 있지 않아 pleos 값 대신 화면 값을 쓴다.
 *   C 338은 portrait_compact (Q&A Q3), scroll_fade 48 (461:14702). 16 + 56 + 16 + 338 + 16 + 368 = 810.
 * - WF: 452:54321 (648×800), 한 열. scroll_fade 461:14704.
 */
private data class PortraitSpec(
    val margin: Dp,
    val vertical: Dp,
    val gap: Dp,
    val rowGutter: Dp,
    /** true면 C → L1 → R1 → L2 → R2 한 열 */
    val singleColumn: Boolean = false,
    val cHeight: Dp,
    /** 한 열에서는 R1 높이 */
    val row1Height: Dp = 368.dp,
    val l1Height: Dp = row1Height,
    /** L2는 펼치면 길어져 최소 높이로 쓴다 */
    val row2Height: Dp = 576.dp,
    val fadeHeight: Dp = 200.dp,
) {
    companion object {
        private val WStandard = PortraitSpec(
            margin = 12.dp, vertical = 16.dp, gap = 16.dp, rowGutter = 20.dp,
            cHeight = 338.dp, fadeHeight = 48.dp,
        )
        private val Wf = PortraitSpec(
            margin = 32.dp, vertical = 16.dp, gap = 16.dp, rowGutter = 16.dp,
            singleColumn = true, cHeight = 666.dp, l1Height = 260.dp,
        )

        fun of(formFactor: FormFactor, area: AreaSpec): PortraitSpec = when (formFactor) {
            FormFactor.W_STANDARD -> WStandard
            FormFactor.WF -> Wf
            FormFactor.S17, FormFactor.W_EXTENDED -> PortraitSpec(
                margin = area.margin, vertical = area.vertical, gap = area.gutter, rowGutter = area.gutter,
                cHeight = 652.dp,
            )
        }
    }
}

@Composable
private fun PortraitLayout(
    spec: PortraitSpec,
    statusBar: @Composable () -> Unit,
    l1: @Composable () -> Unit,
    l2: @Composable () -> Unit,
    c: @Composable () -> Unit,
    r1: @Composable () -> Unit,
    r2: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = spec.margin, vertical = spec.vertical),
            verticalArrangement = Arrangement.spacedBy(spec.gap),
        ) {
            Box(Modifier.fillMaxWidth().height(StatusBarSlotHeight)) { statusBar() }
            Box(Modifier.fillMaxWidth().height(spec.cHeight)) { c() }
            if (spec.singleColumn) {
                Box(Modifier.fillMaxWidth().height(spec.l1Height)) { l1() }
                Box(Modifier.fillMaxWidth().height(spec.row1Height)) { r1() }
                Box(Modifier.fillMaxWidth().heightIn(min = spec.row2Height)) { l2() }
                Box(Modifier.fillMaxWidth().height(spec.row2Height)) { r2() }
            } else {
                // L1(358)이 1행(368)보다 낮아 아래 10이 빈다 (하이파이와 같다)
                Row(horizontalArrangement = Arrangement.spacedBy(spec.rowGutter)) {
                    Box(Modifier.weight(1f).height(spec.row1Height)) { l1() }
                    Box(Modifier.weight(1f).height(spec.row1Height)) { r1() }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(spec.rowGutter)) {
                    // L2는 펼치면(상황 15) 길어져 최소 높이만 준다. R2 아래가 비는 것은 의도다.
                    Box(Modifier.weight(1f).heightIn(min = spec.row2Height)) { l2() }
                    Box(Modifier.weight(1f).height(spec.row2Height)) { r2() }
                }
            }
        }
        val showFade by remember(scrollState) { derivedStateOf { scrollState.canScrollForward } }
        if (showFade) {
            ScrollFade(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(spec.fadeHeight),
            )
        }
    }
}

@Preview(name = "S-17 가로", widthDp = 1700, heightDp = 1184)
@Composable
private fun AlterChargeLayoutLandscapePreview() {
    AlterChargeTheme { PreviewSlots() }
}

@Preview(name = "S-17 가로 · L2 펼침", widthDp = 1700, heightDp = 1184)
@Composable
private fun AlterChargeLayoutLeftScrollPreview() {
    AlterChargeTheme { PreviewSlots(leftColumnScrollable = true) }
}

@Preview(name = "S-17 세로", widthDp = 840, heightDp = 1184)
@Composable
private fun AlterChargeLayoutPortraitPreview() {
    AlterChargeTheme { PreviewSlots() }
}

@Preview(name = "W-Standard 세로", widthDp = 800, heightDp = 810)
@Composable
private fun AlterChargeLayoutWStandardPortraitPreview() {
    AlterChargeTheme { PreviewSlots() }
}

@Preview(name = "WF 세로", widthDp = 648, heightDp = 800)
@Composable
private fun AlterChargeLayoutWfPortraitPreview() {
    AlterChargeTheme { PreviewSlots() }
}

@Preview(name = "WF 가로", widthDp = 1256, heightDp = 800)
@Composable
private fun AlterChargeLayoutWfPreview() {
    AlterChargeTheme { PreviewSlots() }
}

@Composable
private fun PreviewSlots(leftColumnScrollable: Boolean = false) {
    AlterChargeLayout(
        leftColumnScrollable = leftColumnScrollable,
        statusBar = { PreviewSlot("status_bar", Modifier.height(56.dp)) },
        l1 = { PreviewSlot("L1", Modifier.height(368.dp)) },
        // 상황 15 펼친 L2
        l2 = { PreviewSlot("L2", if (leftColumnScrollable) Modifier.height(855.dp) else Modifier) },
        c = { PreviewSlot("C") },
        r1 = { PreviewSlot("R1", Modifier.height(368.dp)) },
        r2 = { PreviewSlot("R2") },
    )
}

@Composable
private fun PreviewSlot(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, AlterTheme.colors.surfaceAccent, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(label, color = AlterTheme.colors.textTertiary)
    }
}
