/** 충전소 연결 타입 이름 (REQ_SL_001 · REQ_ST_007). 서버 ConnectorType 값과 문구를 짝지어 둔다 */
package com.altercharge.app.ui.card.stationitem

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.altercharge.app.R
import com.altercharge.app.data.remote.model.ConnectorType

/**
 * R2 목록 한 줄의 연결 타입 (Figma v2 가이드 V2-5): 대표 1개만 쓰고 나머지는 "외 N"으로 줄인다.
 * 대표는 목록이 앞에 둔 타입이다(필터에서 고른 타입을 앞으로 보낸다). 없으면 null
 */
@Composable
fun connectorSummaryText(types: List<ConnectorType>): String? {
    val shown = types.distinct().filter { it != ConnectorType.UNKNOWN }
    if (shown.isEmpty()) return null
    val first = connectorLabel(shown.first())
    return if (shown.size == 1) first else stringResource(R.string.card_item_connector_more, first, shown.size - 1)
}

@Composable
fun connectorLabel(type: ConnectorType): String = when (type) {
    ConnectorType.AC_SLOW -> stringResource(R.string.card_item_connector_ac_slow)
    ConnectorType.AC_3PHASE -> stringResource(R.string.card_item_connector_ac_three_phase)
    ConnectorType.DC_CHADEMO -> stringResource(R.string.card_item_connector_dc_chademo)
    ConnectorType.DC_COMBO -> stringResource(R.string.card_item_connector_dc_combo)
    ConnectorType.DC_COMBO_SLOW -> stringResource(R.string.card_item_connector_dc_combo_slow)
    ConnectorType.DC_COMBO_2 -> stringResource(R.string.card_item_connector_dc_combo_2)
    ConnectorType.NACS -> stringResource(R.string.card_item_connector_nacs)
    ConnectorType.UNKNOWN -> stringResource(R.string.common_value_unavailable)
}
