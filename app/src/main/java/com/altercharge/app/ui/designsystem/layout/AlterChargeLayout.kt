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
 * 홈 화면의 레이아웃 틀. 카드 내용은 모르고, 슬롯 6개를 폼팩터와 방향에 맞게 배치만 한다.
 *
 * - 가로: 앱 바 자리(80) → L(L1, L2) · C · R(R1, R2). 스크롤 없이 한 화면이다 (Layout Rules · 스크롤).
 *   S-17 높이 합: 32 + 80(상태 바 56 + 빈칸 24) + 1040 + 32 = 1184.
 *   본문 시작 y = 112 (Layout Rules · 상단 영역, 하이파이 body 455:58813과 같다).
 * - 세로: 상태 바 → C → L1 | R1 → L2 | R2, 세로 스크롤 (15상황 하이파이 455:58776 기준).
 *   아래에 scroll_fade(높이 200)를 덮는다.
 *
 * 세로는 부모가 스크롤이라 높이가 무한대가 된다. 그래서 세로 슬롯은 틀이 높이를 정해 준다.
 *
 * @param leftColumnScrollable 가로에서 L2를 펼쳤을 때(상황 15) 왼쪽 열만 스크롤하고 아래에 페이드를 덮는다.
 *   이때 l1 · l2 슬롯은 높이 제약이 무한대라 안에서 fillMaxHeight나 weight를 쓰면 안 된다.
 *   WF는 스크롤 없음 규칙이라 이 값을 무시한다 (FormFactor.landscapeLeftScrollable).
 *
 * 슬롯 안에서는 [LocalAppLayout]으로 이 틀이 고른 폼팩터와 방향을 읽을 수 있다.
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
                PortraitLayout(spec, statusBar, l1, l2, c, r1, r2)
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

/** 세로 하단 페이드 (하이파이 461:14853): 화면 폭 전체, 높이 200, 화면 아래에 붙는다. */
private val PortraitFadeHeight = 200.dp

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
 * 세로 슬롯 높이. S-17 세로 하이파이 기준 (C 652, 1행 368, 2행 576).
 * 다른 폼팩터 세로는 시안이 없어 같은 값을 쓴다. 시안이 나오면 AreaSpec으로 옮긴다.
 * TODO 디자이너 확인: 1행 칸은 368인데 L1 card_info 세로 모드 높이는 358이라 칸 아래 10이 빈다.
 *
 * 세로 위치(S-17 하이파이): 상태 바 y 32 → C y 108 → 1행 y 780 → 2행 y 1168.
 * 세로는 앱 바 자리가 80이 아니라 상태 바 56 + 거터 20이다(본문 y 108). 하이파이를 따른다.
 *
 * 접힘선 규칙(Layout Rules · 첫 화면 행동): 첫 화면에 L1과 "여기로 안내"(R1)가 보여야 한다.
 * Layout Rules 표는 세로 순서를 L1 | R1 → C로 적었지만 하이파이는 C가 먼저다.
 * S-17(1184) · W-Extended(1400)는 1행이 y 1148에서 끝나 접힘선 위라 하이파이 순서를 따른다.
 * TODO 디자이너 확인: W-Standard 세로(높이 810)는 이 순서로는 1행이 접힘선 아래로 내려간다.
 *   시안이 나오면 L1 축소(WF 세로 예: 368 → 260)나 순서 변경을 반영한다.
 */
private val PortraitCHeight = 652.dp
private val PortraitRow1Height = 368.dp
private val PortraitRow2Height = 576.dp

@Composable
private fun PortraitLayout(
    spec: AreaSpec,
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
            verticalArrangement = Arrangement.spacedBy(spec.gutter),
        ) {
            Box(Modifier.fillMaxWidth().height(StatusBarSlotHeight)) { statusBar() }
            Box(Modifier.fillMaxWidth().height(PortraitCHeight)) { c() }
            Row(horizontalArrangement = Arrangement.spacedBy(spec.gutter)) {
                Box(Modifier.weight(1f).height(PortraitRow1Height)) { l1() }
                Box(Modifier.weight(1f).height(PortraitRow1Height)) { r1() }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spec.gutter)) {
                // L2는 펼치면(상황 15) 길어지므로 최소 높이만 준다. R2는 576 그대로 두고 아래는 비운다(의도).
                Box(Modifier.weight(1f).heightIn(min = PortraitRow2Height)) { l2() }
                Box(Modifier.weight(1f).height(PortraitRow2Height)) { r2() }
            }
        }
        // 끝까지 내리면 마지막 내용이 가려지지 않게 숨긴다.
        val showFade by remember(scrollState) { derivedStateOf { scrollState.canScrollForward } }
        if (showFade) {
            ScrollFade(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(PortraitFadeHeight),
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
