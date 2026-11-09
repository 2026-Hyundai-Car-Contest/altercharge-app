package com.altercharge.app.ui.designsystem.layout

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.component.PlaceholderBox
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme

/**
 * 홈 화면의 레이아웃 틀. 카드 내용은 모르고, 슬롯 6개를 폼팩터와 방향에 맞게 배치만 한다.
 *
 * - 가로: 상단 바 → L(3칸: L1, L2) · C(6칸) · R(3칸: R1, R2)
 * - 세로: 상단 바 → C → L1 | R1 → L2 | R2, 세로 스크롤 (15상황 하이파이 455:58776 기준)
 *
 * 세로는 부모가 스크롤이라 높이가 무한대가 된다. 그래서 세로 슬롯은 틀이 높이를 정해 준다.
 *
 * @param leftColumnScrollable 가로에서 L2를 펼쳤을 때(상황 15) 왼쪽 열만 스크롤한다.
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
        if (isLandscape) {
            LandscapeLayout(
                spec, formFactor.landscapeSpans, maxWidth, leftColumnScrollable,
                statusBar, l1, l2, c, r1, r2,
            )
        } else {
            PortraitLayout(spec, statusBar, l1, l2, c, r1, r2)
        }
    }
}

// TODO 서희님: 상단 앱바 자리(+80), 하단 스크롤 페이드는 디자인 값 작업 때 Figma 위치 그대로 맞춘다.

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spec.margin, vertical = spec.vertical),
        verticalArrangement = Arrangement.spacedBy(spec.gutter),
    ) {
        statusBar()
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spec.gutter),
        ) {
            val leftModifier = Modifier.width(spanWidth(spans.left)).fillMaxHeight()
            if (leftColumnScrollable) {
                Column(
                    modifier = leftModifier.verticalScroll(rememberScrollState()),
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
}

@Composable
private fun ColumnScope.FillRest(content: @Composable () -> Unit) {
    Box(Modifier.weight(1f).fillMaxWidth()) { content() }
}

/** 세로 슬롯 높이. S-17 세로 하이파이 기준 (C 652, 1행 368, 2행 576). */
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spec.margin, vertical = spec.vertical),
        verticalArrangement = Arrangement.spacedBy(spec.gutter),
    ) {
        statusBar()
        Box(Modifier.fillMaxWidth().height(PortraitCHeight)) { c() }
        Row(horizontalArrangement = Arrangement.spacedBy(spec.gutter)) {
            Box(Modifier.weight(1f).height(PortraitRow1Height)) { l1() }
            Box(Modifier.weight(1f).height(PortraitRow1Height)) { r1() }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spec.gutter)) {
            // L2는 펼치면(상황 15) 길어지므로 최소 높이만 준다.
            Box(Modifier.weight(1f).heightIn(min = PortraitRow2Height)) { l2() }
            Box(Modifier.weight(1f).height(PortraitRow2Height)) { r2() }
        }
    }
}

@Preview(name = "S-17 가로", widthDp = 1700, heightDp = 1184)
@Composable
private fun AlterChargeLayoutLandscapePreview() {
    AlterChargeTheme { PreviewSlots() }
}

@Preview(name = "S-17 세로", widthDp = 840, heightDp = 1184)
@Composable
private fun AlterChargeLayoutPortraitPreview() {
    AlterChargeTheme { PreviewSlots() }
}

@Composable
private fun PreviewSlots() {
    AlterChargeLayout(
        statusBar = { PlaceholderBox("status_bar", Modifier.height(56.dp)) },
        l1 = { PlaceholderBox("L1", Modifier.height(368.dp)) },
        l2 = { PlaceholderBox("L2") },
        c = { PlaceholderBox("C") },
        r1 = { PlaceholderBox("R1", Modifier.height(368.dp)) },
        r2 = { PlaceholderBox("R2") },
    )
}
