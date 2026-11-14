/** 충전소 연결 타입 이름 (REQ_SL_001 · REQ_ST_007). 서버 ConnectorType 값과 문구를 짝지어 둔다 */
package com.altercharge.app.ui.card.stationitem

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.altercharge.app.R
import com.altercharge.app.data.remote.model.ConnectorType

/** 연결 타입들을 "DC콤보 · AC완속"처럼 잇는다. UNKNOWN은 보여 줄 이름이 없어 뺀다. 없으면 null */
@Composable
fun connectorLabelText(types: List<ConnectorType>): String? {
    val shown = types.distinct().filter { it != ConnectorType.UNKNOWN }
    if (shown.isEmpty()) return null
    val labels = mutableListOf<String>()
    for (type in shown) {
        labels += connectorLabel(type)
    }
    return labels.joinToString(" · ")
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
