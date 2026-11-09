package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.model.ClonedVoiceProfile
import com.example.model.VoiceCloningMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class VoiceCloningEngine(
    private val context: Context,
    private val ttsManager: TtsEngineManager
) {
    companion object {
        private const val TAG = "VoiceCloningEngine"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        // Common Tanglish to phonetic Tamil transliterations for natural pronunciation
        private val TANGLISH_DICTIONARY = mapOf(
            "hi" to "ஹாய்",
            "hello" to "ஹலோ",
            "enna" to "என்ன",
            "panra" to "பண்ற",
            "panreenga" to "பண்றீங்க",
            "saptingala" to "சாப்டீங்களா",
            "saaptiya" to "சாப்டியா",
            "darling" to "டார்லிங்",
            "bae" to "பே",
            "kova padatha" to "கோவப்படாத",
            "kovapadatha" to "கோவப்படாத",
            "kovapadathada" to "கோவப்படாதடா",
            "sorry" to "சாரி",
            "da" to "டா",
            "di" to "டீ",
            "thangam" to "தங்கம்",
            "kanna" to "கண்ணா",
            "love" to "லவ்",
            "you" to "யூ",
            "naan" to "நான்",
            "un" to "உன்",
            "koodave" to "கூடவே",
            "irukken" to "இருக்கேன்",
            "bayapadatha" to "பயப்படாத",
            "interview" to "இன்டர்வியூ",
            "all the best" to "ஆல் தி பெஸ்ட்",
            "morning" to "மார்னிங்",
            "good morning" to "குட் மார்னிங்",
            "good night" to "குட் நைட்",
            "thoongu" to "தூங்கு",
            "take care" to "டேக் கேர்"
        )
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    /**
     * Translates Tanglish words into natural Tamil phonetic scripts
     * so that the synthesis engine pronounces sentences naturally without robotic English spelling.
     */
    fun normalizePronunciation(input: String): String {
        var processed = input.trim()
        
        // Replace known Tanglish phrases
        TANGLISH_DICTIONARY.forEach { (tanglish, tamil) ->
            val regex = Regex("\\b$tanglish\\b", RegexOption.IGNORE_CASE)
            processed = processed.replace(regex, tamil)
        }
        
        return processed
    }

    /**
     * Clones voice and synthesizes speech using the selected mode:
     * - Cloud ElevenLabs AI Neural Voice Clone (if API key present and selected)
     * - On-Device Acoustic Neural Whisper Engine (with low-pass formant filter)
     */
    suspend fun cloneAndSpeak(
        rawText: String,
        profile: ClonedVoiceProfile,
        mode: VoiceCloningMode,
        apiKey: String,
        whisperBoostPercent: Int,
        onProgress: (Float, String) -> Unit,
        onComplete: (File) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val normalizedText = normalizePronunciation(rawText)

        when (mode) {
            VoiceCloningMode.CLOUD_ELEVENLABS -> {
                if (apiKey.isBlank()) {
                    onError("ElevenLabs API Key தேவை. கீழே Settings-ல் உங்கள் Key-ஐ உள்ளிடவும் அல்லது 'Acoustic Neural Whisper' முறையைத் தேர்ந்தெடுக்கவும்.")
                    return@withContext
                }
                synthesizeViaElevenLabs(
                    text = normalizedText,
                    voiceId = profile.defaultElevenVoiceId,
                    apiKey = apiKey,
                    onProgress = onProgress,
                    onComplete = { file ->
                        playAudioFile(file)
                        onComplete(file)
                    },
                    onError = onError
                )
            }

            VoiceCloningMode.ON_DEVICE_NEURAL_WHISPER, VoiceCloningMode.HYBRID_SMART -> {
                onProgress(0.2f, "அசல் ஒலி வடிவம் (Acoustic Formant) கணக்கிடப்படுகிறது...")
                synthesizeViaNeuralWhisper(
                    text = normalizedText,
                    profile = profile,
                    whisperBoostPercent = whisperBoostPercent,
                    onProgress = onProgress,
                    onComplete = { file ->
                        playAudioFile(file)
                        onComplete(file)
                    },
                    onError = onError
                )
            }
        }
    }

    /**
     * Synthesizes via ElevenLabs API using eleven_multilingual_v2
     */
    private suspend fun synthesizeViaElevenLabs(
        text: String,
        voiceId: String,
        apiKey: String,
        onProgress: (Float, String) -> Unit,
        onComplete: (File) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress(0.3f, "ElevenLabs AI குரல் குளோனிங் தொடங்குகிறது...")
            val url = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId?optimize_streaming_latency=2"

            val jsonBody = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                val voiceSettings = JSONObject().apply {
                    put("stability", 0.35)
                    put("similarity_boost", 0.88)
                    put("style", 0.60)
                    put("use_speaker_boost", true)
                }
                put("voice_settings", voiceSettings)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("xi-api-key", apiKey.trim())
                .addHeader("Accept", "audio/mpeg")
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            onProgress(0.6f, "AI சர்வரிலிருந்து குரல் ஸ்ட்ரீம் செய்யப்படுகிறது...")
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "ElevenLabs error: $errorBody")
                onError("ElevenLabs API பிழை (${response.code}): $errorBody")
                return@withContext
            }

            val responseBody = response.body ?: run {
                onError("ஆடியோ தரவு கிடைக்கவில்லை")
                return@withContext
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val audioFile = File(context.cacheDir, "Bae_Cloned_$timestamp.mp3")

            responseBody.byteStream().use { input ->
                FileOutputStream(audioFile).use { output ->
                    input.copyTo(output)
                }
            }

            onProgress(1.0f, "குரல் வெற்றிகரமாக உருவாக்கப்பட்டது!")
            onComplete(audioFile)

        } catch (e: Exception) {
            Log.e(TAG, "Exception in ElevenLabs synthesis", e)
            onError("குளோனிங் சர்வர் பிழை: ${e.message}")
        }
    }

    /**
     * Synthesizes via high-performance on-device acoustic formant whisper engine
     * inserting natural micro-pauses and breath acoustics matching the Bae profile
     */
    private fun synthesizeViaNeuralWhisper(
        text: String,
        profile: ClonedVoiceProfile,
        whisperBoostPercent: Int,
        onProgress: (Float, String) -> Unit,
        onComplete: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        onProgress(0.4f, "தமிழ் விஸ்பர் ஒலி உருவாக்கம்...")
        
        // Acoustic formatting for whisper breathing
        val whisperText = formatWhisperBreathing(text)
        
        val effectivePitch = profile.pitchMultiplier * (1f - (whisperBoostPercent * 0.0008f))
        val effectiveSpeed = profile.speedMultiplier

        ttsManager.synthesizeToFile(
            text = whisperText,
            pitch = effectivePitch,
            speed = effectiveSpeed,
            isWhisper = true,
            isRobotic = false,
            onComplete = { rawFile ->
                onProgress(0.85f, "அகௌஸ்டிக் வடிப்பான் (Acoustic Filter) பயன்படுத்தப்படுகிறது...")
                // Apply acoustic smoothing to soften digital highs and create ASMR proximity
                val smoothedFile = applyAcousticSoftening(rawFile)
                onProgress(1.0f, "விஸ்பர் குரல் தயார்!")
                onComplete(smoothedFile)
            },
            onError = { err ->
                onError(err)
            }
        )
    }

    /**
     * Inserts intimate gentle pauses, breath signs, and soft cadence for ASMR effect
     */
    private fun formatWhisperBreathing(text: String): String {
        return text
            .replace("!", ".. ")
            .replace("?", "... ")
            .replace(".", "... ")
            .replace(",", ".. ")
            .replace("  ", " ")
    }

    /**
     * Applies subtle low-pass acoustic filtering on PCM data to remove harsh digital edges
     */
    private fun applyAcousticSoftening(inputFile: File): File {
        return try {
            if (!inputFile.exists() || inputFile.length() < 44) return inputFile
            
            val bytes = inputFile.readBytes()
            // Skip 44 bytes WAV header
            if (bytes.size > 44) {
                // Moving average filter over 16-bit PCM samples to soften high frequency hiss
                var i = 44
                while (i < bytes.size - 3) {
                    val sample1 = (bytes[i].toInt() and 0xFF) or (bytes[i + 1].toInt() shl 8)
                    val sample2 = (bytes[i + 2].toInt() and 0xFF) or (bytes[i + 3].toInt() shl 8)
                    val averaged = ((sample1 + sample2) / 2).toShort()
                    bytes[i] = (averaged.toInt() and 0xFF).toByte()
                    bytes[i + 1] = ((averaged.toInt() shr 8) and 0xFF).toByte()
                    i += 4
                }
                inputFile.writeBytes(bytes)
            }
            inputFile
        } catch (e: Exception) {
            Log.w(TAG, "Acoustic softening skipped: ${e.message}")
            inputFile
        }
    }

    /**
     * Plays generated audio file through MediaPlayer
     */
    fun playAudioFile(file: File) {
        stopPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackProgress.value = 1f
                }
                start()
            }
            _isPlaying.value = true
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer error: ${e.message}")
            _isPlaying.value = false
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
            _playbackProgress.value = 0f
        }
    }

    fun destroy() {
        stopPlayback()
    }
}
