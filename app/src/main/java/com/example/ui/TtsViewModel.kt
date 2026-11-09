package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TtsEngineManager
import com.example.audio.TtsInitState
import com.example.model.AudioMetadataInfo
import com.example.model.FemaleTone
import com.example.model.SpecialVoiceStyle
import com.example.model.VoiceDepth
import com.example.model.VoiceEmotion
import com.example.model.VoiceModulation
import com.example.model.VoicePresetsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class StudioUiState(
    val inputText: String = VoicePresetsData.sampleTamilTexts.first(),
    val selectedTone: FemaleTone = VoicePresetsData.femaleTones[3], // Female, Warm, Smooth
    val selectedModulation: VoiceModulation = VoicePresetsData.voiceModulations[2], // Medium Pitch
    val selectedStyle: SpecialVoiceStyle = VoicePresetsData.specialVoiceStyles[0], // Girlfriend Style
    val pitchPercent: Int = 78, // 78% as requested in user specification
    val speedMultiplier: Float = 1.00f,
    val voiceDepth: VoiceDepth = VoiceDepth.MEDIUM,
    val voiceEmotion: VoiceEmotion = VoiceEmotion.NORMAL,
    val isExporting: Boolean = false,
    val isPlayingExported: Boolean = false,
    val currentInspectedAudio: AudioMetadataInfo? = null,
    val showDetailsDialog: Boolean = false,
    val exportSuccessMessage: String? = null,
    val previewingItemName: String? = null,
    val savedHistory: List<AudioMetadataInfo> = emptyList()
)

class TtsViewModel(application: Application) : AndroidViewModel(application) {

    val ttsManager = TtsEngineManager(application)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val isSpeaking = ttsManager.isSpeaking
    val initState = ttsManager.initState

    // Live calculated Effective Pitch & Speed
    val calculatedFormula = combine(_uiState) { states ->
        val s = states[0]
        calculateCombinedVoiceOutput(
            tone = s.selectedTone,
            modulation = s.selectedModulation,
            style = s.selectedStyle,
            pitchPercent = s.pitchPercent,
            speedMultiplier = s.speedMultiplier,
            depth = s.voiceDepth,
            emotion = s.voiceEmotion
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Pair(1.0f, 1.0f)
    )

    fun onTextChanged(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun onToneSelected(tone: FemaleTone) {
        _uiState.value = _uiState.value.copy(selectedTone = tone)
    }

    fun onModulationSelected(mod: VoiceModulation) {
        _uiState.value = _uiState.value.copy(selectedModulation = mod)
    }

    fun onStyleSelected(style: SpecialVoiceStyle) {
        _uiState.value = _uiState.value.copy(selectedStyle = style)
    }

    fun onPitchChanged(newPercent: Int) {
        _uiState.value = _uiState.value.copy(pitchPercent = newPercent)
    }

    fun onSpeedChanged(newSpeed: Float) {
        _uiState.value = _uiState.value.copy(speedMultiplier = newSpeed)
    }

    fun onDepthSelected(depth: VoiceDepth) {
        _uiState.value = _uiState.value.copy(voiceDepth = depth)
    }

    fun onEmotionSelected(emotion: VoiceEmotion) {
        _uiState.value = _uiState.value.copy(voiceEmotion = emotion)
    }

    fun dismissDetailsDialog() {
        _uiState.value = _uiState.value.copy(showDetailsDialog = false)
        ttsManager.stopExportedPlayback()
    }

    fun showAudioDetails(info: AudioMetadataInfo) {
        _uiState.value = _uiState.value.copy(
            currentInspectedAudio = info,
            showDetailsDialog = true
        )
    }

    /**
     * Preview single Tone with standard welcome phrase
     */
    fun previewTone(tone: FemaleTone) {
        val previewPhrase = "வணக்கம்! சுபிக்ஷா டீச்சர் செயலிக்கு உங்களை அன்புடன் வரவேற்கிறோம்."
        _uiState.value = _uiState.value.copy(previewingItemName = tone.titleTa)
        ttsManager.speak(
            text = previewPhrase,
            pitch = tone.basePitch,
            speed = tone.baseRate
        )
    }

    /**
     * Preview single Modulation
     */
    fun previewModulation(mod: VoiceModulation) {
        val previewPhrase = "வணக்கம்! சுபிக்ஷா டீச்சர் செயலிக்கு உங்களை அன்புடன் வரவேற்கிறோம்."
        _uiState.value = _uiState.value.copy(previewingItemName = mod.titleTa)
        ttsManager.speak(
            text = previewPhrase,
            pitch = mod.pitchMultiplier,
            speed = mod.speedMultiplier,
            isWhisper = mod.isWhisper,
            isRobotic = mod.isRobotic
        )
    }

    /**
     * Preview single Special Voice Style
     */
    fun previewStyle(style: SpecialVoiceStyle) {
        val previewPhrase = "வணக்கம்! சுபிக்ஷா டீச்சர் செயலிக்கு உங்களை அன்புடன் வரவேற்கிறோம்."
        _uiState.value = _uiState.value.copy(previewingItemName = style.titleTa)
        val stylePitch = (1.0f + style.pitchOffset).coerceIn(0.5f, 2.0f)
        val styleSpeed = (1.0f + style.speedOffset).coerceIn(0.5f, 2.0f)
        ttsManager.speak(
            text = previewPhrase,
            pitch = stylePitch,
            speed = styleSpeed,
            isWhisper = style.breathyEffect
        )
    }

    /**
     * 5. Final Output Voice:
     * Tone + Modulation + Voice Style + Pitch + Speed = Final Tamil Voice Output
     */
    fun speakFinalOutput() {
        val state = _uiState.value
        val (finalPitch, finalSpeed) = calculateCombinedVoiceOutput(
            tone = state.selectedTone,
            modulation = state.selectedModulation,
            style = state.selectedStyle,
            pitchPercent = state.pitchPercent,
            speedMultiplier = state.speedMultiplier,
            depth = state.voiceDepth,
            emotion = state.voiceEmotion
        )

        val isWhisper = state.selectedModulation.isWhisper || state.selectedStyle.breathyEffect
        val isRobotic = state.selectedModulation.isRobotic

        ttsManager.speak(
            text = state.inputText,
            pitch = finalPitch,
            speed = finalSpeed,
            isWhisper = isWhisper,
            isRobotic = isRobotic
        )
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    /**
     * Synthesize and download high-quality lossless WAV file
     */
    fun exportLosslessWav() {
        val state = _uiState.value
        val (finalPitch, finalSpeed) = calculateCombinedVoiceOutput(
            tone = state.selectedTone,
            modulation = state.selectedModulation,
            style = state.selectedStyle,
            pitchPercent = state.pitchPercent,
            speedMultiplier = state.speedMultiplier,
            depth = state.voiceDepth,
            emotion = state.voiceEmotion
        )

        _uiState.value = _uiState.value.copy(isExporting = true)

        val isWhisper = state.selectedModulation.isWhisper || state.selectedStyle.breathyEffect
        val isRobotic = state.selectedModulation.isRobotic

        ttsManager.synthesizeToFile(
            text = state.inputText,
            pitch = finalPitch,
            speed = finalSpeed,
            isWhisper = isWhisper,
            isRobotic = isRobotic,
            onComplete = { cacheFile ->
                val fileName = "Subiksha_TamilTTS_${System.currentTimeMillis()}.wav"
                val exportedUri = ttsManager.saveToDownloads(cacheFile, fileName)

                val audioDetails = ttsManager.inspectWav(
                    file = cacheFile,
                    toneTitle = state.selectedTone.titleTa,
                    modulationTitle = state.selectedModulation.titleTa,
                    styleTitle = state.selectedStyle.titleTa,
                    pitch = finalPitch,
                    speed = finalSpeed,
                    depth = state.voiceDepth.titleTa,
                    emotion = state.voiceEmotion.titleTa,
                    textSnippet = state.inputText,
                    exportedUri = exportedUri
                )

                val updatedHistory = listOf(audioDetails) + state.savedHistory

                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    currentInspectedAudio = audioDetails,
                    showDetailsDialog = true,
                    savedHistory = updatedHistory,
                    exportSuccessMessage = "ஆடியோ Downloads கோப்புறையில் தரமாகப் பதிவிறக்கப்பட்டது!"
                )
            },
            onError = { err ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccessMessage = "பதிவிறக்கப் பிழை: $err"
                )
            }
        )
    }

    fun playInspectedAudio(audio: AudioMetadataInfo) {
        val file = File(audio.filePath)
        if (file.exists()) {
            ttsManager.playAudioFile(file) {
                _uiState.value = _uiState.value.copy(isPlayingExported = false)
            }
            _uiState.value = _uiState.value.copy(isPlayingExported = true)
        }
    }

    fun stopInspectedAudio() {
        ttsManager.stopExportedPlayback()
        _uiState.value = _uiState.value.copy(isPlayingExported = false)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.destroy()
    }

    companion object {
        /**
         * Mathematical Output Voice Formula:
         * Tone + Modulation + Voice Style + Pitch + Speed = Final Tamil Voice Output
         */
        fun calculateCombinedVoiceOutput(
            tone: FemaleTone,
            modulation: VoiceModulation,
            style: SpecialVoiceStyle,
            pitchPercent: Int,
            speedMultiplier: Float,
            depth: VoiceDepth,
            emotion: VoiceEmotion
        ): Pair<Float, Float> {
            // Pitch slider: 50% is 1.0x, 78% is ~1.14x, 100% is 1.25x, 0% is 0.75x
            val pitchSliderFactor = 0.75f + (pitchPercent / 100f) * 0.5f

            val basePitch = tone.basePitch
            val modPitch = modulation.pitchMultiplier
            val stylePitch = 1.0f + style.pitchOffset
            val depthPitch = depth.pitchFactor
            val emotionPitch = emotion.pitchFactor

            // Combined Pitch
            val combinedPitch = (basePitch * modPitch * stylePitch * depthPitch * emotionPitch * (pitchSliderFactor / 1.0f))
                .coerceIn(0.5f, 2.0f)

            // Speed calculation
            val baseRate = tone.baseRate
            val modSpeed = modulation.speedMultiplier
            val styleSpeed = 1.0f + style.speedOffset
            val emotionSpeed = emotion.speedFactor

            val combinedSpeed = (baseRate * modSpeed * styleSpeed * emotionSpeed * speedMultiplier)
                .coerceIn(0.25f, 3.0f)

            return Pair(combinedPitch, combinedSpeed)
        }
    }
}
