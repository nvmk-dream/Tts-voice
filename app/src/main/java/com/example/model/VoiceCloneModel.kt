package com.example.model

import java.io.File

enum class VoiceCloningMode(val titleTa: String, val subtitleEn: String) {
    INDIC_F5("AI4Bharat IndicF5 (IIT Madras)", "Zero-Shot Voice Clone (100% Free & Accurate)"),
    CLOUD_ELEVENLABS("ElevenLabs Cloud AI", "Neural Voice with Free Tier API Key"),
    ON_DEVICE_NEURAL_WHISPER("Acoustic Neural Whisper", "On-Device Formant Filter (Offline)")
}

data class ClonedVoiceProfile(
    val id: String,
    val nameTa: String,
    val nameEn: String,
    val sourceAudioTitle: String,
    val sourceFileName: String,
    val assetAudioPath: String,
    val refWavAssetPath: String,
    val refTranscript: String,
    val descriptionTa: String,
    val sampleRateHz: Int = 24000,
    val pitchMultiplier: Float = 0.78f,
    val speedMultiplier: Float = 0.95f,
    val whisperIntimacyPercent: Int = 98,
    val tags: List<String> = listOf("IndicF5", "IIT Madras", "ASMR Whisper", "100% Cloned"),
    val defaultElevenVoiceId: String = "EXAVITQu4vr4xnSDxMaL", // Default female intimate voice ID
    val avatarEmoji: String = "🎙️",
    val samplePhrases: List<String> = listOf(
        "டார்லிங், கோவப்படாதடா.. நான் உன் கூடவே இருக்கேன்.",
        "Hi hello enna panra? சாப்பிட்டியா?",
        "இன்னைக்கு இன்டர்வியூ நல்லா பண்ணுவ, பயப்படாதடா என் தங்கம்.",
        "மெதுவா கண்ணை மூடு.. எல்லாம் சரியாகிடும்."
    )
)

data class VoiceCloneState(
    val isGenerating: Boolean = false,
    val isPlaying: Boolean = false,
    val currentProgress: Float = 0f,
    val statusMessage: String = "AI4Bharat IndicF5 வாய்ஸ் குளோனிங் தயார்",
    val selectedProfile: ClonedVoiceProfile,
    val selectedMode: VoiceCloningMode = VoiceCloningMode.INDIC_F5,
    val elevenLabsApiKey: String = "sk_e69b01863c2d40ecacf4876db10c7c8a198c7b2ae9b4cb32", // Pre-configured with user's verified key
    val whisperBoostPercent: Int = 85,
    val emotionalIntimacyPercent: Int = 92,
    val lastGeneratedFile: File? = null,
    val error: String? = null
)

object VoiceClonePresets {
    val profiles = listOf(
        ClonedVoiceProfile(
            id = "bae_calms_you",
            nameTa = "Bae - சமாதானம் விஸ்பர் (அசல்)",
            nameEn = "Bae - Calms You After Fight",
            sourceAudioTitle = "Bae calms you after a fight - Tamil ASMR",
            sourceFileName = "Bae_calms_you_after_a_fight_-_Tamil_ASMR_f9be9688.opus",
            assetAudioPath = "asmr/bae_calms_you.opus",
            refWavAssetPath = "asmr/ref_bae_calm.wav",
            refTranscript = "சாரி டா.. உன்னை காயப்படுத்தணும்னு நினைக்கல.",
            descriptionTa = "கோவத்தை தனித்து அமைதியாக்கும் மிக நெருக்கமான தமிழ் காதல் விஸ்பர் குரல். IIT Madras IndicF5 மூலம் 100% Zero-Shot குளோன் செய்யப்பட்டது.",
            pitchMultiplier = 0.78f,
            speedMultiplier = 0.92f,
            whisperIntimacyPercent = 98,
            tags = listOf("IndicF5 Clone", "After Fight", "Whisper ASMR", "Calm"),
            avatarEmoji = "💖",
            samplePhrases = listOf(
                "டார்லிங், கோவப்படாதடா.. நான் உன் கூடவே இருக்கேன்.",
                "சாரி டா.. உன்னை காயப்படுத்தணும்னு நினைக்கல.",
                "மெதுவா என் கையை பிடிச்சுக்கோ, மனசு சரியாயிடும்.",
                "சாப்டியா கண்ணா? உடம்ப பாத்துக்கோ."
            )
        ),
        ClonedVoiceProfile(
            id = "bae_job_interview",
            nameTa = "Bae Comfort - காலை ஊக்கம் & ஆறுதல்",
            nameEn = "Bae Comfort - Job Interview Morning Whisper",
            sourceAudioTitle = "Job interview comfort - Tamil ASMR",
            sourceFileName = "Job_interview_comfort.__tamilasmr__tamil__asmrcommunity__asmrforsleep__tamilasmrstories_cf3f96f3.opus",
            assetAudioPath = "asmr/job_interview_comfort.opus",
            refWavAssetPath = "asmr/ref_bae_interview.wav",
            refTranscript = "இன்னைக்கு இன்டர்வியூ நல்லா பண்ணுவ, பயப்படாதடா என் தங்கம்.",
            descriptionTa = "காலை விடியலில் இன்டர்வியூ அல்லது சவாலான நாளுக்கு ஊக்கமளிக்கும் பாசமான விஸ்பர் குரல். IndicF5 Zero-Shot Cloned.",
            pitchMultiplier = 0.82f,
            speedMultiplier = 0.96f,
            whisperIntimacyPercent = 94,
            tags = listOf("IndicF5 Clone", "Morning Whisper", "Motivation", "Bae"),
            avatarEmoji = "☕",
            samplePhrases = listOf(
                "இன்னைக்கு இன்டர்வியூ நல்லா பண்ணுவ, பயப்படாதடா என் தங்கம்.",
                "நீ திறமையானவன் டா, கண்டிப்பா ஜெயிப்பாய்.",
                "டீ குடிச்சிட்டு கிளம்பு, ஆல் தி வெரி பெஸ்ட்!"
            )
        )
    )
}
