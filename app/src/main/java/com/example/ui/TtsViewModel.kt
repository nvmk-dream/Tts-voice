package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TtsEngineManager
import com.example.model.AudioMetadataInfo
import com.example.model.CharacterVoice
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
import java.io.File
import java.util.UUID

data class StudioUiState(
    val currentTab: Int = 0, // 0 = FineVoice & ASMR Characters, 1 = Studio & Build, 2 = Saved Audio
    val inputText: String = VoicePresetsData.sampleTamilTexts.first(),
    val characterVoices: List<CharacterVoice> = VoicePresetsData.characterVoices,
    val activeCharacter: CharacterVoice? = VoicePresetsData.characterVoices.first(),
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
    val showBuildVoiceDialog: Boolean = false,
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

    fun onTabChanged(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(currentTab = tabIndex)
    }

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

    fun openBuildVoiceDialog() {
        _uiState.value = _uiState.value.copy(showBuildVoiceDialog = true)
    }

    fun dismissBuildVoiceDialog() {
        _uiState.value = _uiState.value.copy(showBuildVoiceDialog = false)
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
     * Play sample quote for Character Voice (FineVoice style)
     */
    fun playCharacterSample(character: CharacterVoice) {
        val tone = VoicePresetsData.femaleTones.find { it.id == character.toneId } ?: _uiState.value.selectedTone
        val mod = VoicePresetsData.voiceModulations.find { it.id == character.modulationId } ?: _uiState.value.selectedModulation
        val style = VoicePresetsData.specialVoiceStyles.find { it.id == character.styleId } ?: _uiState.value.selectedStyle

        val (pitch, speed) = calculateCombinedVoiceOutput(
            tone = tone,
            modulation = mod,
            style = style,
            pitchPercent = character.defaultPitchPercent,
            speedMultiplier = character.defaultSpeed,
            depth = VoiceDepth.MEDIUM,
            emotion = if (character.isAsmr) VoiceEmotion.SOFT else VoiceEmotion.NORMAL
        )

        _uiState.value = _uiState.value.copy(previewingItemName = character.nameTa)

        ttsManager.speak(
            text = character.sampleQuoteTa,
            pitch = pitch,
            speed = speed,
            isWhisper = character.isAsmr || mod.isWhisper || style.breathyEffect,
            isRobotic = mod.isRobotic
        )
    }

    /**
     * "✦ Use" button from FineVoice Character card:
     * Immediately applies this character's recipe and switches to Studio tab
     */
    fun useCharacterVoice(character: CharacterVoice) {
        val tone = VoicePresetsData.femaleTones.find { it.id == character.toneId } ?: _uiState.value.selectedTone
        val mod = VoicePresetsData.voiceModulations.find { it.id == character.modulationId } ?: _uiState.value.selectedModulation
        val style = VoicePresetsData.specialVoiceStyles.find { it.id == character.styleId } ?: _uiState.value.selectedStyle

        _uiState.value = _uiState.value.copy(
            activeCharacter = character,
            selectedTone = tone,
            selectedModulation = mod,
            selectedStyle = style,
            pitchPercent = character.defaultPitchPercent,
            speedMultiplier = character.defaultSpeed,
            voiceEmotion = if (character.isAsmr) VoiceEmotion.SOFT else VoiceEmotion.NORMAL,
            currentTab = 1, // Switch to Studio Tab
            exportSuccessMessage = "${character.nameTa} குரல் தேர்ந்தெடுக்கப்பட்டது! Studio-வில் பேசிப் பார்க்கலாம்."
        )

        // Give immediate audio feedback
        playCharacterSample(character)
    }

    /**
     * Save and Build a brand new custom Voice Profile
     */
    fun saveAndBuildCustomVoice(nameTa: String, roleDesc: String) {
        val s = _uiState.value
        val newVoice = CharacterVoice(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            nameEn = "Custom Voice (${s.selectedTone.titleEn})",
            nameTa = nameTa,
            ageRole = roleDesc,
            descriptionTa = "பயனரால் சுயமாக உருவாக்கப்பட்ட தமிழ் பெண் குரல் (Tone: ${s.selectedTone.titleTa}, Style: ${s.selectedStyle.titleTa})",
            tags = listOf("Custom Built", "Original", "Tamil", if (s.selectedModulation.isWhisper) "ASMR" else "Studio"),
            sampleQuoteTa = s.inputText.take(80),
            usedCount = "1 Used",
            avatarColor = 0xFF059669,
            avatarInitial = nameTa.take(1),
            toneId = s.selectedTone.id,
            modulationId = s.selectedModulation.id,
            styleId = s.selectedStyle.id,
            defaultPitchPercent = s.pitchPercent,
            defaultSpeed = s.speedMultiplier,
            isAsmr = s.selectedModulation.isWhisper || s.selectedStyle.breathyEffect,
            isCustomBuilt = true
        )

        val updatedList = listOf(newVoice) + s.characterVoices

        _uiState.value = s.copy(
            characterVoices = updatedList,
            activeCharacter = newVoice,
            showBuildVoiceDialog = false,
            exportSuccessMessage = "'$nameTa' குரல் வெற்றிகரமாக உருவாக்கப்பட்டு சேமிக்கப்பட்டது!"
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
        fun calculateCombinedVoiceOutput(
            tone: FemaleTone,
            modulation: VoiceModulation,
            style: SpecialVoiceStyle,
            pitchPercent: Int,
            speedMultiplier: Float,
            depth: VoiceDepth,
            emotion: VoiceEmotion
        ): Pair<Float, Float> {
            val pitchSliderFactor = 0.75f + (pitchPercent / 100f) * 0.5f

            val basePitch = tone.basePitch
            val modPitch = modulation.pitchMultiplier
            val stylePitch = 1.0f + style.pitchOffset
            val depthPitch = depth.pitchFactor
            val emotionPitch = emotion.pitchFactor

            val combinedPitch = (basePitch * modPitch * stylePitch * depthPitch * emotionPitch * (pitchSliderFactor / 1.0f))
                .coerceIn(0.5f, 2.0f)

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
