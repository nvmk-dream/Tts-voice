package com.example.model

import android.net.Uri

/**
 * 1. Female Voice - Tone வகைகள் (15 வகை பெண் குரல் தன்மைகள்)
 */
data class FemaleTone(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val descriptionTa: String,
    val category: String, // "General", "Professional", "Emotional", "Storytelling", "Youth"
    val basePitch: Float,
    val baseRate: Float
)

/**
 * 2. Voice Modulation - குரல் மாற்றங்கள் (10 வகைகள்)
 */
data class VoiceModulation(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val pitchMultiplier: Float,
    val speedMultiplier: Float,
    val descriptionTa: String,
    val cadencePauseMs: Long = 0L,
    val isRobotic: Boolean = false,
    val isWhisper: Boolean = false
)

/**
 * 3. Special Female Voice Styles (14 கூடுதல் பாணிகள்)
 */
data class SpecialVoiceStyle(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val descriptionTa: String,
    val pitchOffset: Float,
    val speedOffset: Float,
    val breathyEffect: Boolean = false,
    val warmIntimate: Boolean = false
)

enum class VoiceDepth(val titleTa: String, val pitchFactor: Float) {
    LIGHT("Light (மெல்லிய)", 1.08f),
    MEDIUM("Medium (இயல்பான)", 1.00f),
    DEEP("Deep (ஆழமான)", 0.88f)
}

enum class VoiceEmotion(val titleTa: String, val pitchFactor: Float, val speedFactor: Float) {
    SOFT("Soft (மென்மையான)", 0.95f, 0.92f),
    NORMAL("Normal (வழக்கமான)", 1.00f, 1.00f),
    EXPRESSIVE("Expressive (வெளிப்படையான)", 1.08f, 1.05f)
}

/**
 * Detailed Metadata for Exported Lossless Audio
 */
data class AudioMetadataInfo(
    val fileName: String,
    val filePath: String,
    val fileUri: Uri? = null,
    val fileSizeFormatted: String,
    val durationFormatted: String,
    val sampleRateHz: Int,
    val channels: Int,
    val bitDepth: Int,
    val bitrateKbps: Int,
    val formatDescription: String,
    val createdTimeFormatted: String,
    // Applied Studio Parameters
    val appliedToneTitle: String,
    val appliedModulationTitle: String,
    val appliedStyleTitle: String,
    val finalPitch: Float,
    val finalSpeed: Float,
    val appliedDepth: String,
    val appliedEmotion: String,
    val sourceTextSnippet: String
)

object VoicePresetsData {

    val femaleTones = listOf(
        FemaleTone(
            id = "expressive",
            titleEn = "Female, Expressive",
            titleTa = "உணர்ச்சிப்பூர்வமான குரல்",
            descriptionTa = "இயற்கையான உணர்வுகளுடன் உயிரோட்டமாகப் பேசுதல்",
            category = "Emotional",
            basePitch = 1.08f,
            baseRate = 1.02f
        ),
        FemaleTone(
            id = "younger_expressive",
            titleEn = "Female, Younger, Expressive",
            titleTa = "இளமையான துடிப்பான குரல்",
            descriptionTa = "இளமை துள்ளும் உற்சாகமான தமிழ் குரல்",
            category = "Youth",
            basePitch = 1.18f,
            baseRate = 1.08f
        ),
        FemaleTone(
            id = "child_expressive",
            titleEn = "Female, Child, Expressive",
            titleTa = "மழலைக் குரல்",
            descriptionTa = "மழலை கொஞ்சும் கனிவான குழந்தை போன்ற பேச்சு",
            category = "Youth",
            basePitch = 1.34f,
            baseRate = 1.06f
        ),
        FemaleTone(
            id = "warm_smooth",
            titleEn = "Female, Warm, Smooth",
            titleTa = "கதகதப்பான மென் குரல்",
            descriptionTa = "நெஞ்சை வருடும் மென்மையான இதமான ஒலி",
            category = "General",
            basePitch = 0.98f,
            baseRate = 0.95f
        ),
        FemaleTone(
            id = "calm_soft",
            titleEn = "Female, Calm, Soft",
            titleTa = "அமைதியான மென் குரல்",
            descriptionTa = "மன அமைதி தரும் நிதானமான மெல்லிய குரல்",
            category = "General",
            basePitch = 0.95f,
            baseRate = 0.88f
        ),
        FemaleTone(
            id = "friendly_conversational",
            titleEn = "Female, Friendly, Conversational",
            titleTa = "நட்பான உரையாடல் குரல்",
            descriptionTa = "நண்பருடன் நேரில் பேசுவது போன்ற இயல்பான பாணி",
            category = "General",
            basePitch = 1.02f,
            baseRate = 1.00f
        ),
        FemaleTone(
            id = "sweet_gentle",
            titleEn = "Female, Sweet, Gentle",
            titleTa = "இனிமையான கனிவான குரல்",
            descriptionTa = "பண்பான மற்றும் இனிமை நிறைந்த உச்சரிப்பு",
            category = "General",
            basePitch = 1.10f,
            baseRate = 0.94f
        ),
        FemaleTone(
            id = "cheerful_happy",
            titleEn = "Female, Cheerful, Happy",
            titleTa = "மகிழ்ச்சியான குரல்",
            descriptionTa = "புன்னகையுடன் பாசிட்டிவ் எனர்ஜியுடன் பேசுதல்",
            category = "Emotional",
            basePitch = 1.14f,
            baseRate = 1.05f
        ),
        FemaleTone(
            id = "professional_clear",
            titleEn = "Female, Professional, Clear",
            titleTa = "தொழில்முறை தெளிவான குரல்",
            descriptionTa = "அறிவிப்புகள், பாடங்கள் மற்றும் செய்திகளுக்கான தெளிவு",
            category = "Professional",
            basePitch = 1.00f,
            baseRate = 1.00f
        ),
        FemaleTone(
            id = "confident_strong",
            titleEn = "Female, Confident, Strong",
            titleTa = "தன்னம்பிக்கை கம்பீரக் குரல்",
            descriptionTa = "உறுதியும் தைரியமும் வெளிப்படும் அழுத்தமான ஒலி",
            category = "Professional",
            basePitch = 0.96f,
            baseRate = 1.02f
        ),
        FemaleTone(
            id = "soothing_relaxed",
            titleEn = "Female, Soothing, Relaxed",
            titleTa = "ஆறுதலான குரல்",
            descriptionTa = "இளைப்பாறும் உணர்வைத் தரும் இதமான குரல்",
            category = "General",
            basePitch = 0.92f,
            baseRate = 0.85f
        ),
        FemaleTone(
            id = "energetic_lively",
            titleEn = "Female, Energetic, Lively",
            titleTa = "சுறுசுறுப்பான உற்சாகக் குரல்",
            descriptionTa = "துடிப்புடனும் ஆர்வத்துடனும் வேகமான பேச்சு",
            category = "Emotional",
            basePitch = 1.16f,
            baseRate = 1.12f
        ),
        FemaleTone(
            id = "storytelling_emotional",
            titleEn = "Female, Storytelling, Emotional",
            titleTa = "கதை சொல்லும் குரல்",
            descriptionTa = "கதை மாந்தர்களை கண்முன் நிறுத்தும் நயமான பாணி",
            category = "Storytelling",
            basePitch = 1.04f,
            baseRate = 0.90f
        ),
        FemaleTone(
            id = "mature_elegant",
            titleEn = "Female, Mature, Elegant",
            titleTa = "முதிர்ச்சியான நேர்த்தியான குரல்",
            descriptionTa = "அனுபவமும் கண்ணியமும் கொண்ட கம்பீரப் பேச்சு",
            category = "Professional",
            basePitch = 0.88f,
            baseRate = 0.95f
        ),
        FemaleTone(
            id = "natural_conversational",
            titleEn = "Female, Natural, Conversational",
            titleTa = "இயல்பான தமிழ் பேச்சு",
            descriptionTa = "தினசரி உரையாடல்களுக்கான தூய தமிழ் பேச்சு",
            category = "General",
            basePitch = 1.00f,
            baseRate = 0.98f
        )
    )

    val voiceModulations = listOf(
        VoiceModulation(
            id = "high_pitch",
            titleEn = "High Pitch",
            titleTa = "உயர்ந்த குரல்",
            pitchMultiplier = 1.25f,
            speedMultiplier = 1.00f,
            descriptionTa = "உயர்ந்த ஸ்வரத்தில் ஒலிக்கும் தெளிவு"
        ),
        VoiceModulation(
            id = "low_pitch",
            titleEn = "Low Pitch",
            titleTa = "ஆழமான குரல்",
            pitchMultiplier = 0.82f,
            speedMultiplier = 0.98f,
            descriptionTa = "கனமான அடிக்குரல் பாணி"
        ),
        VoiceModulation(
            id = "medium_pitch",
            titleEn = "Medium Pitch",
            titleTa = "இயல்பான குரல்",
            pitchMultiplier = 1.00f,
            speedMultiplier = 1.00f,
            descriptionTa = "இயல்பான நடுநிலை ஸ்வரம்"
        ),
        VoiceModulation(
            id = "slow_speech",
            titleEn = "Slow Speech",
            titleTa = "மெதுவான பேச்சு",
            pitchMultiplier = 0.98f,
            speedMultiplier = 0.75f,
            descriptionTa = "நிதானமாக ஒவ்வொரு சொல்லையும் விளக்குதல்"
        ),
        VoiceModulation(
            id = "fast_speech",
            titleEn = "Fast Speech",
            titleTa = "வேகமான பேச்சு",
            pitchMultiplier = 1.04f,
            speedMultiplier = 1.30f,
            descriptionTa = "விரைவான சுறுசுறுப்பான அறிவிப்பு"
        ),
        VoiceModulation(
            id = "dynamic_pitch",
            titleEn = "Dynamic Pitch",
            titleTa = "ஏற்ற இறக்கமான குரல்",
            pitchMultiplier = 1.10f,
            speedMultiplier = 1.04f,
            descriptionTa = "வார்த்தைக்கு வார்த்தை மாறும் ஸ்வர நயம்"
        ),
        VoiceModulation(
            id = "emotional",
            titleEn = "Emotional",
            titleTa = "உணர்ச்சியுடன் பேசுதல்",
            pitchMultiplier = 1.06f,
            speedMultiplier = 0.92f,
            descriptionTa = "ஆழ்ந்த நெகிழ்ச்சியான உணர்வு"
        ),
        VoiceModulation(
            id = "robotic",
            titleEn = "Robotic",
            titleTa = "ஒரே சீரான குரல்",
            pitchMultiplier = 0.92f,
            speedMultiplier = 1.05f,
            descriptionTa = "மாற்றமில்லாத நவீன ரோபோ பாணி",
            isRobotic = true
        ),
        VoiceModulation(
            id = "whisper",
            titleEn = "Whisper",
            titleTa = "கிசுகிசுப்பான குரல்",
            pitchMultiplier = 0.86f,
            speedMultiplier = 0.88f,
            descriptionTa = "ரகசியமான மெல்லிய காற்று கலந்த ஒலி",
            isWhisper = true
        ),
        VoiceModulation(
            id = "dramatic",
            titleEn = "Dramatic",
            titleTa = "நாடகத்தன்மையான குரல்",
            pitchMultiplier = 1.12f,
            speedMultiplier = 0.88f,
            descriptionTa = "நாடக மேடை மற்றும் சினிமா பாணி வெளிப்பாடு"
        )
    )

    val specialVoiceStyles = listOf(
        SpecialVoiceStyle(
            id = "girlfriend",
            titleEn = "Girlfriend Style",
            titleTa = "காதலியைப் போல் அன்பாகப் பேசுதல்",
            descriptionTa = "கனிவும் அன்பும் கொஞ்சலும் கலந்த இனிமையான குரல்",
            pitchOffset = 0.12f,
            speedOffset = -0.04f,
            warmIntimate = true
        ),
        SpecialVoiceStyle(
            id = "wife",
            titleEn = "Wife Style",
            titleTa = "மனைவியைப் போல் நெருக்கமாகப் பேசுதல்",
            descriptionTa = "பாசமும் அக்கறையும் நிறைந்த குடும்பக் குரல்",
            pitchOffset = 0.04f,
            speedOffset = -0.02f,
            warmIntimate = true
        ),
        SpecialVoiceStyle(
            id = "affectionate",
            titleEn = "Affectionate",
            titleTa = "பாசமாகப் பேசுதல்",
            descriptionTa = "மனமார்ந்த தாய்மை/அன்பு பொழியும் குரல்",
            pitchOffset = 0.08f,
            speedOffset = -0.08f,
            warmIntimate = true
        ),
        SpecialVoiceStyle(
            id = "romantic",
            titleEn = "Romantic",
            titleTa = "காதல் உணர்வுடன் பேசுதல்",
            descriptionTa = "மெல்லிய இசை போன்ற காதல் உணர்ச்சி நயம்",
            pitchOffset = 0.02f,
            speedOffset = -0.12f,
            warmIntimate = true
        ),
        SpecialVoiceStyle(
            id = "husky",
            titleEn = "Husky Voice",
            titleTa = "லேசான கரகரப்புடன் கூடிய குரல்",
            descriptionTa = "கவர்ச்சிகரமான லேசான கரகரப்பு கலந்த குரல்",
            pitchOffset = -0.12f,
            speedOffset = -0.05f
        ),
        SpecialVoiceStyle(
            id = "deep_female",
            titleEn = "Deep Female Voice",
            titleTa = "ஆழமான பெண் குரல்",
            descriptionTa = "அடிக்குரலில் ஒலிக்கும் காந்தக் குரல்",
            pitchOffset = -0.22f,
            speedOffset = -0.04f
        ),
        SpecialVoiceStyle(
            id = "raspy",
            titleEn = "Raspy Voice",
            titleTa = "கரகரப்பான குரல்",
            descriptionTa = "தனித்துவமான கடின கரகரப்புத் தன்மை",
            pitchOffset = -0.15f,
            speedOffset = 0.02f
        ),
        SpecialVoiceStyle(
            id = "breathless_soft",
            titleEn = "Breathless Soft Voice",
            titleTa = "மூச்சுக் கலந்த மென்மையான குரல்",
            descriptionTa = "மென்மையான மூச்சிரைப்பு கலந்த இதமான குரல்",
            pitchOffset = 0.05f,
            speedOffset = -0.14f,
            breathyEffect = true
        ),
        SpecialVoiceStyle(
            id = "warm_intimate",
            titleEn = "Warm Intimate Voice",
            titleTa = "கதகதப்பான நெருக்கமான குரல்",
            descriptionTa = "செவிக்கு மிக அருகில் பேசுவது போன்ற நெருக்கம்",
            pitchOffset = -0.06f,
            speedOffset = -0.10f,
            warmIntimate = true
        ),
        SpecialVoiceStyle(
            id = "seductive",
            titleEn = "Seductive Voice",
            titleTa = "கவர்ச்சியான குரல்",
            descriptionTa = "மயக்கும் நயமும் அழுத்தமும் கொண்ட பாணி",
            pitchOffset = -0.08f,
            speedOffset = -0.16f,
            warmIntimate = true
        ),
        SpecialVoiceStyle(
            id = "mature_husky",
            titleEn = "Mature Husky Voice",
            titleTa = "முதிர்ச்சியான கரகரப்பான குரல்",
            descriptionTa = "முதிர்ந்த அனுபவமிக்க கம்பீர கரகரப்பு",
            pitchOffset = -0.18f,
            speedOffset = -0.08f
        ),
        SpecialVoiceStyle(
            id = "sweet_whisper",
            titleEn = "Sweet Whisper",
            titleTa = "இனிமையான கிசுகிசுப்பான குரல்",
            descriptionTa = "இனிமையும் மென்மையும் கொண்ட கிசுகிசுப்பு",
            pitchOffset = 0.14f,
            speedOffset = -0.12f,
            breathyEffect = true
        ),
        SpecialVoiceStyle(
            id = "bold_female",
            titleEn = "Bold Female Voice",
            titleTa = "கம்பீரமான பெண் குரல்",
            descriptionTa = "அதிகாரமும் தன்னம்பிக்கையும் கொண்ட சிங்கக் குரல்",
            pitchOffset = -0.04f,
            speedOffset = 0.08f
        ),
        SpecialVoiceStyle(
            id = "playful",
            titleEn = "Playful Voice",
            titleTa = "குறும்புத்தனமான குரல்",
            descriptionTa = "குறும்புத்தனமும் சிரிப்பும் கலந்த உல்லாசக் குரல்",
            pitchOffset = 0.20f,
            speedOffset = 0.12f
        )
    )

    val sampleTamilTexts = listOf(
        "வணக்கம்! சுபிக்ஷா டீச்சர் செயலிக்கு உங்களை அன்புடன் வரவேற்கிறோம்.",
        "அகர முதல எழுத்தெல்லாம் ஆதி பகவன் முதற்றே உலகு. கற்க கசடறக் கற்பவை கற்றபின் நிற்க அதற்குத் தக.",
        "இன்று வானிலை மிகவும் இனிமையாக உள்ளது. தென்றல் காற்று மெல்ல வீசுகிறது, மனதுக்கு அமைதி தருகிறது.",
        "அன்பு மாணவச் செல்வங்களே! இன்றைய கணித மற்றும் அறிவியல் பாடங்களை நாம் மகிழ்ச்சியுடன் தொடங்குவோம்.",
        "நிலவொளியில் பூத்த மலர்களைப் போல உங்கள் புன்னகை என்றும் மலர்ந்திருக்கட்டும்."
    )
}
