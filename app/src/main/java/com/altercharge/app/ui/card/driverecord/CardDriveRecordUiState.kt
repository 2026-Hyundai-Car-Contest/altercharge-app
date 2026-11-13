package com.altercharge.app.ui.card.driverecord

import androidx.compose.runtime.Immutable
import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import java.time.LocalDate

/** L2 주간 주행 기록. Figma card_drive_record (366:38474) */
@Immutable
data class CardDriveRecordUiState(
    /** 월요일 */
    val weekStart: LocalDate,
    /** 월~일 7칸. 아직 안 온 날은 null */
    val dailyKm: List<Int?>,
    /** 0 = 월. 지난 주를 보고 있으면 null */
    val todayIndex: Int?,
    val averageKm: Int,
    val totalKm: Int,
    val daysSinceLastCharge: Int?,
    val kmSinceLastCharge: Int?,
    /** 덜 달렸으면 음수 */
    val diffFromLastWeekKm: Int?,
    val hasPreviousWeek: Boolean,
    val hasNextWeek: Boolean,
    /** 상황 15. 화면에 들어올 때는 접힘으로 시작한다(시안 규칙) */
    val expanded: Boolean,
    /**
     * Figma state = empty (이번 주 기록 0건). 이전 · 다음 주 버튼은 눌리지 않고 펼침 버튼 · 상세 통계는 숨긴다.
     * 값은 [dailyKm] 모두 null · [todayIndex] 지정으로 둔다.
     */
    val empty: Boolean = false,
)

/** Figma card_drive_record mode. 크기는 접힘 상태 카드 */
enum class CardDriveRecordMode {
    /** S-17 가로 383×648 */
    LANDSCAPE,

    /** W-Extended 가로 375×816 */
    LANDSCAPE_TALL,

    /** S-17 · W-Extended 세로 378×576 */
    PORTRAIT,

    /** WF 가로 387×455 */
    WF_LANDSCAPE,

    /** WF 세로 584×576 (452:54439, Q&A Q4로 580 → 584) */
    WF_PORTRAIT,
}

/**
 * TODO 디자이너 확인: W-Standard 가로(높이 810)는 시안이 없어 S-17 가로(LANDSCAPE)로 대신한다.
 */
fun cardDriveRecordModeFor(info: AppLayoutInfo): CardDriveRecordMode = when (info.formFactor) {
    FormFactor.WF -> if (info.isLandscape) CardDriveRecordMode.WF_LANDSCAPE else CardDriveRecordMode.WF_PORTRAIT
    FormFactor.W_EXTENDED -> if (info.isLandscape) CardDriveRecordMode.LANDSCAPE_TALL else CardDriveRecordMode.PORTRAIT
    FormFactor.S17, FormFactor.W_STANDARD ->
        if (info.isLandscape) CardDriveRecordMode.LANDSCAPE else CardDriveRecordMode.PORTRAIT
}
