package com.altercharge.app.ui.designsystem.layout

import android.annotation.SuppressLint
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
 * 홈 화면의 레이아웃 틀. 카드 내용은 모르고, 슬롯 6개를 폼팩터와 방향에 맞게 배치만 한다.
 *
 * - 가로: 앱 바 자리(80) → L(L1, L2) · C · R(R1, R2). 스크롤 없이 한 화면이다 (Layout Rules · 스크롤).
 *   S-17 높이 합: 32 + 80(상태 바 56 + 빈칸 24) + 1040 + 32 = 1184.
 *   본문 시작 y = 112 (Layout Rules · 상단 영역, 하이파이 body 455:58813과 같다).
 * - 세로: 상태 바 → C → L1 | R1 → L2 | R2, 세로 스크롤 (15상황 하이파이 455:58776 기준).
 *   아래에 scroll_fade(높이 200, W-Standard 48)를 덮는다. WF 세로는 C → L1 → R1 → L2 → R2 한 열이다.
 *   세로 여백 · 간격은 폼팩터별 하이파이 화면 값을 따른다 ([PortraitSpec]).
 *
 * 세로는 부모가 스크롤이라 높이가 무한대가 된다. 그래서 세로 슬롯은 틀이 높이를 정해 준다.
 *
 * @param leftColumnScrollable 가로에서 L2를 펼쳤을 때(상황 15) 왼쪽 열만 스크롤하고 아래에 페이드를 덮는다.
 *   이때 l1 · l2 슬롯은 높이 제약이 무한대라 안에서 fillMaxHeight나 weight를 쓰면 안 된다.
 *   WF는 스크롤 없음 규칙이라 이 값을 무시한다 (FormFactor.landscapeLeftScrollable).
 *
 * 슬롯 안에서는 [LocalAppLayout]으로 이 틀이 고른 폼팩터와 방향을 읽을 수 있다.
 *
 * 이 틀은 화면 배경을 칠하지 않는다. 화면 쪽에서 AlterChargeTheme 안에
 * Box(Modifier.fillMaxSize().background(AlterTheme.colors.screenBackground))로 감싼다.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
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

/** 상태 바 자리 높이 (하이파이 status_bar 56). */
private val StatusBarSlotHeight = 56.dp

/** 가로 앱 바 자리. 위 여백 아래 80을 비우고 본문을 놓는다 (Layout Rules · 상단 영역). */
private val LandscapeAppBarArea = 80.dp

/** 가로 L2 펼침 페이드 (하이파이 상황 15, 461:16766): 높이 160, 왼쪽 열보다 좌우로 8씩 넓다. */
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
    // 열 너비는 컬럼 그리드로 계산한다. S-17 가로: L 383 · C 790 · R 383 (하이파이와 같다)
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
        // 하이파이 상황 15: 페이드는 화면 아래 끝에 붙고(아래 여백까지 덮음) 왼쪽 열만 가린다.
        // 끝까지 내리면 마지막 내용이 가려지지 않게 숨긴다.
        // 스크롤 위치가 바뀔 때마다 전체를 다시 그리지 않도록, 끝 도달 여부가 바뀔 때만 읽는다.
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

/**
 * 스크롤이 더 있다는 표시. 하이파이 scroll_fade 그라데이션:
 * 위 투명 → 55% 지점 78% → 아래 96% 불투명. 색은 화면 배경(screenBackground)이라 끝이 배경과 이어진다.
 * 터치를 받지 않는 그림이라 아래 카드 조작을 막지 않는다.
 */
@Composable
private fun ScrollFade(modifier: Modifier) {
    // 다크 페이드는 시안에 없어 화면 배경 다크 값을 쓴다 (라이트는 시안 #EDEEF2와 같다).
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
 *
 * - 기본(S-17 · W-Extended): S-17 세로 하이파이 기준. 여백 · 간격은 AreaSpec, C 652, 1행 368, 2행 576, 페이드 200 (461:14853).
 *   위치: 상태 바 y 32 → C y 108 → 1행 y 780 → 2행 y 1168 (앱 바 자리 80이 아니라 상태 바 56 + 거터 20).
 *   1행 칸은 368이고 L1 card_info 세로는 358이라 칸 아래 10이 빈다 (하이파이 화면도 같다: row_1 368 안에 L1 358).
 * - W-Standard: 화면 altercharge/home/24_6_connect_w_standard/portrait (452:53649, 800×810) 값.
 *   이 화면은 layout 변수가 연결돼 있지 않아 pleos 값(margin 32 · gutter 20 · app_padding 24) 대신 화면 값을 쓴다:
 *   좌우 12 · 위 16 · 세로 간격 16 · 행 안 가로 간격 20(378 + 20 + 378) · C 776×338(portrait_compact, Q&A Q3) ·
 *   1행 368 · 2행 576 · scroll_fade 48 (461:14702). 16 + 56 + 16 + 338 + 16 + 368 = 810으로 L1 · R1이 첫 화면에 들어온다.
 * - WF: 화면 altercharge/home/18_5_waterfall/portrait (452:54321, 648×800) 값. 한 열 배치:
 *   좌우 32 · 위 16 · 간격 16 · 상태 바 584×56 → C 666 → L1 260 → R1 368 → L2 576 → R2 576 · scroll_fade 200 (461:14704).
 *
 * 접힘선 규칙(Layout Rules · 첫 화면 행동): 첫 화면에 L1과 "여기로 안내"(R1)가 보여야 한다.
 * 순서는 하이파이대로 C가 먼저다. S-17(1184) · W-Extended(1400) · W-Standard(810)는 1행이 접힘선 위에서 끝난다.
 */
private data class PortraitSpec(
    /** 좌우 여백 */
    val margin: Dp,
    /** 위아래 여백 */
    val vertical: Dp,
    /** 세로 간격 (상태 바 → C → 행 사이) */
    val gap: Dp,
    /** 2열 행 안 가로 간격 */
    val rowGutter: Dp,
    /** true면 C → L1 → R1 → L2 → R2 한 열 */
    val singleColumn: Boolean = false,
    val cHeight: Dp,
    /** 2열: 1행(L1 | R1) 높이. 한 열: R1 높이 */
    val row1Height: Dp = 368.dp,
    /** 한 열에서 L1 높이 */
    val l1Height: Dp = row1Height,
    /** 2행(L2 | R2) 높이. L2는 펼치면 길어져 최소 높이로 쓴다 */
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
                // 칸 안은 위 정렬이라 L1(358)이 1행(368)보다 낮으면 아래가 빈다 (하이파이와 같다).
                Row(horizontalArrangement = Arrangement.spacedBy(spec.rowGutter)) {
                    Box(Modifier.weight(1f).height(spec.row1Height)) { l1() }
                    Box(Modifier.weight(1f).height(spec.row1Height)) { r1() }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(spec.rowGutter)) {
                    // L2는 펼치면(상황 15) 길어지므로 최소 높이만 준다. R2는 576 그대로 두고 아래는 비운다(의도).
                    Box(Modifier.weight(1f).heightIn(min = spec.row2Height)) { l2() }
                    Box(Modifier.weight(1f).height(spec.row2Height)) { r2() }
                }
            }
        }
        // 끝까지 내리면 마지막 내용이 가려지지 않게 숨긴다.
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
        // 상황 15: 펼친 L2 높이 855
        l2 = { PreviewSlot("L2", if (leftColumnScrollable) Modifier.height(855.dp) else Modifier) },
        c = { PreviewSlot("C") },
        r1 = { PreviewSlot("R1", Modifier.height(368.dp)) },
        r2 = { PreviewSlot("R2") },
    )
}

/** 미리보기 슬롯 자리 표시 (디버그 카탈로그의 PlaceholderBox와 같은 모양) */
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
