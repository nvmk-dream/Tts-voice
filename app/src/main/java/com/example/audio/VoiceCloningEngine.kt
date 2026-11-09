package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.example.model.ClonedVoiceProfile
import com.example.model.VoiceCloningMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class VoiceCloningEngine(
    private val context: Context,
    private val ttsManager: TtsEngineManager
) {
    companion object {
        private const val TAG = "VoiceCloningEngine"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val INDICF5_BASE_URL = "https://ai4bharat-indicf5.hf.space"

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
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    // Cache of uploaded reference audio paths on IndicF5 space to avoid re-uploading
    private val uploadedRefPathCache = mutableMapOf<String, String>()

    /**
     * Translates Tanglish words into natural Tamil phonetic scripts
     */
    fun normalizePronunciation(input: String): String {
        var processed = input.trim()
        TANGLISH_DICTIONARY.forEach { (tanglish, tamil) ->
            val regex = Regex("\\b$tanglish\\b", RegexOption.IGNORE_CASE)
            processed = processed.replace(regex, tamil)
        }
        return processed
    }

    /**
     * Synthesizes voice via the selected cloning engine:
     * 1. INDIC_F5: AI4Bharat Zero-Shot Tamil Voice Clone (IIT Madras)
     * 2. CLOUD_ELEVENLABS: ElevenLabs Multilingual Neural Voice
     * 3. ON_DEVICE_NEURAL_WHISPER: Local Acoustic Formant Filter
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
            VoiceCloningMode.INDIC_F5 -> {
                synthesizeViaIndicF5(
                    text = normalizedText,
                    profile = profile,
                    onProgress = onProgress,
                    onComplete = { file ->
                        playAudioFile(file)
                        onComplete(file)
                    },
                    onError = { err ->
                        Log.w(TAG, "IndicF5 error: $err, falling back to ElevenLabs/Acoustic")
                        onError(err)
                    }
                )
            }

            VoiceCloningMode.CLOUD_ELEVENLABS -> {
                val effectiveKey = apiKey.ifBlank { "sk_e69b01863c2d40ecacf4876db10c7c8a198c7b2ae9b4cb32" }
                synthesizeViaElevenLabs(
                    text = normalizedText,
                    voiceId = profile.defaultElevenVoiceId,
                    apiKey = effectiveKey,
                    onProgress = onProgress,
                    onComplete = { file ->
                        playAudioFile(file)
                        onComplete(file)
                    },
                    onError = onError
                )
            }

            VoiceCloningMode.ON_DEVICE_NEURAL_WHISPER -> {
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
     * AI4Bharat IndicF5 (IIT Madras) Zero-Shot Voice Cloning
     */
    private suspend fun synthesizeViaIndicF5(
        text: String,
        profile: ClonedVoiceProfile,
        onProgress: (Float, String) -> Unit,
        onComplete: (File) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress(0.15f, "AI4Bharat IndicF5 (IIT Madras) சர்வர் இணைக்கப்படுகிறது...")

            // 1. Get or upload reference audio to Gradio Space
            val refServerPath = getOrUploadReferenceAudio(profile) { uploadProgress ->
                onProgress(0.2f + (uploadProgress * 0.2f), "அசல் குரல் மாதிரி (Reference Audio) அனுப்பப்படுகிறது...")
            }

            if (refServerPath == null) {
                onError("Reference ஆடியோ அப்லோட் தோல்வி")
                return@withContext
            }

            onProgress(0.45f, "IIT Madras IndicF5 ஜீரோ-ஷாட் வாய்ஸ் குளோனிங் தொடங்குகிறது...")

            // 2. Call synthesize_speech endpoint
            val callUrl = "$INDICF5_BASE_URL/gradio_api/call/synthesize_speech"
            val payload = JSONObject().apply {
                val dataArr = JSONArray()
                dataArr.put(text) // Component 0: Text to Synthesize

                val audioFileObj = JSONObject().apply {
                    put("path", refServerPath)
                    put("orig_name", "ref_audio.wav")
                    put("url", "$INDICF5_BASE_URL/gradio_api/file=$refServerPath")
                    put("meta", JSONObject().apply { put("_type", "gradio.FileData") })
                }
                dataArr.put(audioFileObj) // Component 1: Reference Audio

                dataArr.put(profile.refTranscript) // Component 2: Text in Reference Prompt
                put("data", dataArr)
            }

            val callRequest = Request.Builder()
                .url(callUrl)
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val callResponse = okHttpClient.newCall(callRequest).execute()
            if (!callResponse.isSuccessful) {
                val errBody = callResponse.body?.string() ?: "Call failed"
                onError("IndicF5 அழைப்பு பிழை (${callResponse.code}): $errBody")
                return@withContext
            }

            val callJson = JSONObject(callResponse.body?.string() ?: "{}")
            val eventId = callJson.optString("event_id")
            if (eventId.isBlank()) {
                onError("IndicF5 Event ID பெற முடியவில்லை")
                return@withContext
            }

            onProgress(0.65f, "குரல் அலைவரிசைகள் (Neural Flow-Matching) உருவாக்கப்படுகின்றன...")

            // 3. Listen to SSE stream until complete
            val streamUrl = "$INDICF5_BASE_URL/gradio_api/call/synthesize_speech/$eventId"
            val streamRequest = Request.Builder().url(streamUrl).build()
            val streamResponse = okHttpClient.newCall(streamRequest).execute()

            var generatedAudioPath: String? = null
            var errorMsg: String? = null

            val reader = BufferedReader(InputStreamReader(streamResponse.body?.byteStream()))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val l = line?.trim() ?: continue
                if (l.startsWith("data:")) {
                    val dataJsonStr = l.substringAfter("data:").trim()
                    try {
                        val arr = JSONArray(dataJsonStr)
                        if (arr.length() > 0) {
                            val firstItem = arr.getJSONObject(0)
                            generatedAudioPath = firstItem.optString("path")
                            break
                        }
                    } catch (e: Exception) {
                        // Not final array
                    }
                } else if (l.startsWith("event: error")) {
                    errorMsg = "IndicF5 உருவாக்கத்தில் பிழை ஏற்பட்டது"
                    break
                }
            }
            reader.close()

            if (generatedAudioPath == null) {
                onError(errorMsg ?: "குரல் ஆடியோ ஃபைல் முகவரி கிடைக்கவில்லை")
                return@withContext
            }

            onProgress(0.9f, "24 kHz உயர்தர ஆடியோ பதிவிறக்கப்படுகிறது...")

            // 4. Download generated 24kHz Lossless WAV
            val audioDownloadUrl = "$INDICF5_BASE_URL/gradio_api/file=$generatedAudioPath"
            val downloadRequest = Request.Builder().url(audioDownloadUrl).build()
            val downloadResponse = okHttpClient.newCall(downloadRequest).execute()

            if (!downloadResponse.isSuccessful || downloadResponse.body == null) {
                onError("ஆடியோ டவுன்லோட் பிழை: ${downloadResponse.code}")
                return@withContext
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val audioFile = File(context.cacheDir, "IndicF5_Bae_${timestamp}.wav")

            downloadResponse.body!!.byteStream().use { input ->
                FileOutputStream(audioFile).use { output ->
                    input.copyTo(output)
                }
            }

            onProgress(1.0f, "IndicF5 குளோன் குரல் தயார்!")
            onComplete(audioFile)

        } catch (e: Exception) {
            Log.e(TAG, "Exception in IndicF5 synthesis", e)
            onError("IndicF5 பிழை: ${e.message}")
        }
    }

    /**
     * Uploads the reference audio from app assets to the Gradio Space if not already cached
     */
    private fun getOrUploadReferenceAudio(
        profile: ClonedVoiceProfile,
        onUploadProgress: (Float) -> Unit
    ): String? {
        val cacheKey = profile.refWavAssetPath
        uploadedRefPathCache[cacheKey]?.let { return it }

        return try {
            onUploadProgress(0.3f)
            // Copy asset to cache file first
            val tempRefFile = File(context.cacheDir, "ref_${profile.id}.wav")
            context.assets.open(profile.refWavAssetPath).use { input ->
                FileOutputStream(tempRefFile).use { output ->
                    input.copyTo(output)
                }
            }

            onUploadProgress(0.6f)
            val uploadUrl = "$INDICF5_BASE_URL/gradio_api/upload"
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "files",
                    "ref_audio.wav",
                    tempRefFile.asRequestBody("audio/wav".toMediaType())
                )
                .build()

            val uploadRequest = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()

            val uploadResponse = okHttpClient.newCall(uploadRequest).execute()
            if (!uploadResponse.isSuccessful) {
                Log.e(TAG, "Gradio upload failed: ${uploadResponse.code}")
                return null
            }

            val responseBody = uploadResponse.body?.string() ?: return null
            val jsonArr = JSONArray(responseBody)
            if (jsonArr.length() > 0) {
                val serverPath = jsonArr.getString(0)
                uploadedRefPathCache[cacheKey] = serverPath
                onUploadProgress(1.0f)
                serverPath
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getOrUploadReferenceAudio", e)
            null
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
            onProgress(0.3f, "ElevenLabs AI குரல் உருவாக்கம் தொடங்குகிறது...")
            val url = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId?optimize_streaming_latency=2"

            val jsonBody = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                val voiceSettings = JSONObject().apply {
                    put("stability", 0.40)
                    put("similarity_boost", 0.85)
                    put("style", 0.55)
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

            onProgress(0.6f, "ElevenLabs AI சர்வரிலிருந்து குரல் பெறப்படுகிறது...")
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
            val audioFile = File(context.cacheDir, "ElevenLabs_Bae_$timestamp.mp3")

            responseBody.byteStream().use { input ->
                FileOutputStream(audioFile).use { output ->
                    input.copyTo(output)
                }
            }

            onProgress(1.0f, "ElevenLabs குரல் வெற்றிகரமாக உருவாக்கப்பட்டது!")
            onComplete(audioFile)

        } catch (e: Exception) {
            Log.e(TAG, "Exception in ElevenLabs synthesis", e)
            onError("ElevenLabs சர்வர் பிழை: ${e.message}")
        }
    }

    /**
     * Synthesizes via high-performance on-device acoustic formant whisper engine
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
                onProgress(1.0f, "விஸ்பர் குரல் தயார்!")
                onComplete(rawFile)
            },
            onError = { err ->
                onError(err)
            }
        )
    }

    private fun formatWhisperBreathing(text: String): String {
        return text
            .replace("!", ".. ")
            .replace("?", "... ")
            .replace(".", "... ")
            .replace(",", ".. ")
            .replace("  ", " ")
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
