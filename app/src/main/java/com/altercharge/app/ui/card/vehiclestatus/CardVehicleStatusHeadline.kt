/** C 카드 헤드라인 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
internal fun Headline(
    text: String,
    maxLines: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = AlterTheme.typography.headlineLarge,
    lineBreak: LineBreak = LineBreak.Unspecified,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontWeight = AlterFontWeight.Strong, lineBreak = lineBreak),
        color = AlterTheme.colors.textPrimary,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** 줄 수를 넘으면 [styles] 순서대로 글자를 줄인다 (Q&A Q1). 가장 작아도 넘치면 말줄임 */
@Composable
internal fun AutoShrinkHeadline(
    text: String,
    styles: List<TextStyle>,
    modifier: Modifier = Modifier,
    maxLines: Int = AUTO_SHRINK_HEADLINE_MAX_LINES,
    lineBreak: LineBreak = PortraitHeadlineLineBreak,
) {
    val measurer = rememberTextMeasurer()
    // fontScale이 바뀌면 다시 재야 한다
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        val maxWidth = constraints.maxWidth
        val candidates = remember(styles, lineBreak) {
            styles.map { it.copy(fontWeight = AlterFontWeight.Strong, lineBreak = lineBreak) }
        }
        val style = remember(text, candidates, maxWidth, maxLines, density) {
            candidates.firstOrNull { candidate ->
                !measurer.measure(
                    text = text,
                    style = candidate,
                    maxLines = maxLines,
                    constraints = Constraints(maxWidth = maxWidth),
                ).hasVisualOverflow
            } ?: candidates.last()
        }
        Text(
            text = text,
            style = style,
            color = AlterTheme.colors.textPrimary,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Figma portrait · portrait_tall · portrait_compact 헤드라인 2줄 */
private const val AUTO_SHRINK_HEADLINE_MAX_LINES = 2

internal const val LANDSCAPE_HEADLINE_MAX_LINES = 2

/** card_vehicle_status/headline/font_size_max 60 → font_size_min 40 */
@Composable
internal fun portraitHeadlineStyles(): List<TextStyle> {
    val typography = AlterTheme.typography
    return listOf(typography.headlineLarge, typography.headlineMedium, typography.headlineSmall, typography.titleLarge)
}

/** 어절 단위 줄바꿈 */
internal val PortraitHeadlineLineBreak = LineBreak(
    strategy = LineBreak.Strategy.HighQuality,
    strictness = LineBreak.Strictness.Normal,
    wordBreak = LineBreak.WordBreak.Phrase,
)
