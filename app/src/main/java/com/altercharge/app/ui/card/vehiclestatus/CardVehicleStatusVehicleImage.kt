/**
 * 차량 이미지(VehicleImage, 공개)와 종류별 에셋 · DRIVING 자르기 값.
 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.altercharge.app.R
import kotlin.math.roundToInt

/**
 * 차량 이미지. 종류별 에셋(이슈 #4)을 주어진 자리 크기에 맞춰 그린다.
 * 자리 크기는 시안에서 회전한 뒤 화면에 차지하는 크기라 이미지를 돌리지 않고 가운데에 맞춘다.
 * DRIVING은 자리 비율이 Figma 슬롯(774×411)과 같다는 전제로 자른다. 자리 비율을 바꾸면 이미지가 늘어난다.
 */
@Composable
fun VehicleImage(kind: VehicleImageKind, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.card_vehicle_status_vehicle_image)
    if (kind == VehicleImageKind.DRIVING) {
        // Figma vehicle_driving(371:9513)은 같은 1440×444 파일을 CROP으로 쓴다. 자리의 (u, v) → 이미지의
        // (0.2138 + 0.5723u, -0.2144 + v): 가로는 21.4%~78.6%만 보이고, 세로는 자리 위 21.4%가 비고 아래가 잘린다.
        val bitmap = ImageBitmap.imageResource(kind.drawableRes)
        Canvas(modifier.clipToBounds().semantics { contentDescription = description }) {
            val dstWidth = size.width / DRIVING_CROP_SCALE_X
            val dstHeight = size.height / DRIVING_CROP_SCALE_Y
            drawImage(
                image = bitmap,
                dstOffset = IntOffset((-DRIVING_CROP_OFFSET_X * dstWidth).roundToInt(), (-DRIVING_CROP_OFFSET_Y * dstHeight).roundToInt()),
                dstSize = IntSize(dstWidth.roundToInt(), dstHeight.roundToInt()),
                filterQuality = FilterQuality.High,
            )
        }
        return
    }
    Image(
        painter = painterResource(kind.drawableRes),
        contentDescription = description,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/** Figma vehicle_driving imageTransform [[0.5723, 0, 0.2138], [0, 1, -0.2144]] */
private const val DRIVING_CROP_SCALE_X = 0.5723f
private const val DRIVING_CROP_OFFSET_X = 0.2138f
private const val DRIVING_CROP_SCALE_Y = 1f
private const val DRIVING_CROP_OFFSET_Y = -0.2144f

@get:DrawableRes
private val VehicleImageKind.drawableRes: Int
    get() = when (this) {
        VehicleImageKind.CHARGING -> R.drawable.img_vehicle_charging
        VehicleImageKind.DRIVING -> R.drawable.img_vehicle_driving
        VehicleImageKind.TOP_VIEW -> R.drawable.img_vehicle_top_view
    }
