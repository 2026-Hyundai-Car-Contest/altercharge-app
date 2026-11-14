/**
 * 충전소 음성 검색 (REQ_SL_003). 결과는 텍스트 검색(REQ_SL_002)과 같은 경로로 보낸다.
 * Pleos SpeechToText는 아직 Coming Soon이라 Android SpeechRecognizer를 쓴다.
 * 마이크 권한은 처음 누를 때 요청하고, 거부하면 다시 묻지 않고 안내만 한다.
 */
package com.altercharge.app.ui.station

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.util.Locale

/**
 * 음성을 한 번 듣는 동작을 돌려준다. 돌려준 함수를 마이크 버튼에서 부른다.
 * 권한 · 인식기 · 인식 실패는 [onError]로 알린다.
 */
@Composable
fun rememberVoiceSearch(
    onResult: (String) -> Unit,
    onListening: () -> Unit,
    onError: (StationListMessage) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val latestResult by rememberUpdatedState(onResult)
    val latestListening by rememberUpdatedState(onListening)
    val latestError by rememberUpdatedState(onError)

    val listener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                latestListening()
            }

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                latestError(
                    when (error) {
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> StationListMessage.VOICE_PERMISSION
                        else -> StationListMessage.VOICE_ERROR
                    },
                )
            }

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.trim()
                    .orEmpty()
                if (text.isEmpty()) latestError(StationListMessage.VOICE_ERROR) else latestResult(text)
            }

            override fun onPartialResults(partialResults: Bundle?) = Unit

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }
    }

    val search = remember(context) { VoiceSearch(context, listener) }
    DisposableEffect(search) {
        onDispose { search.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            latestError(StationListMessage.VOICE_PERMISSION)
        } else if (!search.start()) {
            latestError(StationListMessage.VOICE_UNAVAILABLE)
        }
    }

    return remember(search, permissionLauncher, context) {
        {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            when {
                !granted -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                !search.start() -> latestError(StationListMessage.VOICE_UNAVAILABLE)
            }
        }
    }
}

/** SpeechRecognizer 생성 · 해제를 한 곳에서 맡는다. 없으면 start가 false */
private class VoiceSearch(
    private val context: Context,
    private val listener: RecognitionListener,
) {
    private var recognizer: SpeechRecognizer? = null

    fun start(): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return false
        val recognizer = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(listener)
            this.recognizer = it
        }
        recognizer.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                // 충전소 이름 · 주소가 한국어 데이터라 앱이 영어여도 한국어로 듣는다 (영어로 들으면 검색이 맞지 않는다)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            },
        )
        return true
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}
