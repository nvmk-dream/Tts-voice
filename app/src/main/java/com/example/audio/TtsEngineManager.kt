package com.example.audio

import android.content.ContentValues
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.model.AudioMetadataInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class TtsInitState {
    object Initializing : TtsInitState()
    data class Ready(val availableVoices: List<Voice>, val isTamilSupported: Boolean, val activeVoiceName: String) : TtsInitState()
    data class Error(val message: String) : TtsInitState()
}

class TtsEngineManager(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TtsEngineManager"
        val TAMIL_LOCALE = Locale("ta", "IN")
    }

    private var tts: TextToSpeech? = null

    private val _initState = MutableStateFlow<TtsInitState>(TtsInitState.Initializing)
    val initState: StateFlow<TtsInitState> = _initState.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingProgress = MutableStateFlow(0f)
    val speakingProgress: StateFlow<Float> = _speakingProgress.asStateFlow()

    // Standalone media player for previewing exported files
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlayingExported = MutableStateFlow(false)
    val isPlayingExported: StateFlow<Boolean> = _isPlayingExported.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return
            
            // Set speech audio attributes
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            engine.setAudioAttributes(audioAttributes)

            var langResult = engine.setLanguage(TAMIL_LOCALE)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Try generic Tamil
                langResult = engine.setLanguage(Locale("ta"))
            }

            val isTamilSupported = !(langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED)

            val tamilVoices = mutableListOf<Voice>()
            var bestVoice: Voice? = null

            try {
                val allVoices = engine.voices ?: emptySet()
                for (v in allVoices) {
                    if (v.locale.language == "ta") {
                        tamilVoices.add(v)
                        // Prefer female voices: name contains "female", "taf", "tag", or features
                        val nameLower = v.name.lowercase()
                        if (nameLower.contains("female") || nameLower.contains("taf") || nameLower.contains("ta-in-x-taf")) {
                            bestVoice = v
                        }
                    }
                }
                if (bestVoice == null && tamilVoices.isNotEmpty()) {
                    bestVoice = tamilVoices.first()
                }
                if (bestVoice != null) {
                    engine.voice = bestVoice
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error querying system voices: ${e.message}")
            }

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _speakingProgress.value = 0.2f
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _speakingProgress.value = 1.0f
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _speakingProgress.value = 0f
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    _speakingProgress.value = 0f
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    // Update speaking progress dynamically
                    _speakingProgress.value = 0.6f
                }
            })

            _initState.value = TtsInitState.Ready(
                availableVoices = tamilVoices,
                isTamilSupported = isTamilSupported,
                activeVoiceName = bestVoice?.name ?: "Default System Voice"
            )
        } else {
            _initState.value = TtsInitState.Error("TTS Engine initialization failed (Code: $status)")
        }
    }

    /**
     * Speak text with calculated pitch, speed, and subtle prosodic pauses
     */
    fun speak(
        text: String,
        pitch: Float,
        speed: Float,
        isWhisper: Boolean = false,
        isRobotic: Boolean = false
    ) {
        val engine = tts ?: return
        stop()

        // Clamp values to safe boundaries supported by Android TTS
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        val clampedSpeed = speed.coerceIn(0.25f, 3.0f)

        engine.setPitch(clampedPitch)
        engine.setSpeechRate(clampedSpeed)

        val processedText = formatTamilSpeechText(text, isWhisper, isRobotic)

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, if (isWhisper) 0.65f else 1.0f)
        }

        val utteranceId = "speak_" + UUID.randomUUID().toString()
        engine.speak(processedText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        stopExportedPlayback()
    }

    /**
     * Synthesize audio to uncompressed lossless WAV file
     */
    fun synthesizeToFile(
        text: String,
        pitch: Float,
        speed: Float,
        isWhisper: Boolean = false,
        isRobotic: Boolean = false,
        onComplete: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        val engine = tts ?: run {
            onError("TTS Engine not ready")
            return
        }

        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        val clampedSpeed = speed.coerceIn(0.25f, 3.0f)

        engine.setPitch(clampedPitch)
        engine.setSpeechRate(clampedSpeed)

        val processedText = formatTamilSpeechText(text, isWhisper, isRobotic)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val cacheFile = File(context.cacheDir, "Subiksha_Voice_$timestamp.wav")

        val utteranceId = "synth_" + UUID.randomUUID().toString()

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, if (isWhisper) 0.70f else 1.0f)
        }

        // Setup one-shot listener for file synthesis
        val originalListener = engine
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}

            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    // Check file validity and repair WAV header if synthesized as raw PCM
                    ensureValidWavHeader(cacheFile)
                    onComplete(cacheFile)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    onError("Failed to synthesize audio file")
                }
            }

            override fun onError(id: String?, errorCode: Int) {
                if (id == utteranceId) {
                    onError("Audio synthesis error code: $errorCode")
                }
            }
        })

        val result = engine.synthesizeToFile(processedText, params, cacheFile, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            onError("TTS engine failed to start synthesis (Code: $result)")
        }
    }

    /**
     * Exports the WAV file to user's MediaStore Downloads directory with zero-compression
     */
    fun saveToDownloads(file: File, displayName: String): Uri? {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Subiksha_TTS_Audio")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val uri = resolver.insert(collection, contentValues) ?: return null

            resolver.openOutputStream(uri)?.use { out ->
                FileInputStream(file).use { input ->
                    input.copyTo(out)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            uri
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to Downloads: ${e.message}", e)
            null
        }
    }

    /**
     * Inspects WAV headers to extract exact bitrate, sample rate, channels, bit-depth, duration
     */
    fun inspectWav(
        file: File,
        toneTitle: String,
        modulationTitle: String,
        styleTitle: String,
        pitch: Float,
        speed: Float,
        depth: String,
        emotion: String,
        textSnippet: String,
        exportedUri: Uri? = null
    ): AudioMetadataInfo {
        var sampleRate = 22050
        var channels = 1
        var bitDepth = 16
        var durationMs = 0L

        try {
            if (file.exists() && file.length() >= 44) {
                val bytes = ByteArray(44)
                FileInputStream(file).use { it.read(bytes) }

                val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

                // Check "RIFF"
                val riff = String(bytes, 0, 4)
                if (riff == "RIFF") {
                    channels = buffer.getShort(22).toInt().coerceAtLeast(1)
                    sampleRate = buffer.getInt(24).coerceAtLeast(8000)
                    bitDepth = buffer.getShort(34).toInt().coerceAtLeast(8)

                    val audioDataSize = file.length() - 44
                    val bytesPerSec = sampleRate * channels * (bitDepth / 8)
                    if (bytesPerSec > 0) {
                        durationMs = (audioDataSize * 1000L) / bytesPerSec
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "WAV Inspection fallback: ${e.message}")
        }

        val bitrateKbps = (sampleRate * channels * bitDepth) / 1000
        val fileSizeKb = file.length() / 1024.0
        val sizeFormatted = if (fileSizeKb > 1024) {
            String.format(Locale.getDefault(), "%.2f MB", fileSizeKb / 1024.0)
        } else {
            String.format(Locale.getDefault(), "%.1f KB", fileSizeKb)
        }

        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val millis = (durationMs % 1000) / 100
        val durationFormatted = String.format(Locale.getDefault(), "%02d:%02d.%d வி", minutes, seconds, millis)

        val timeFormatted = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date(file.lastModified()))

        return AudioMetadataInfo(
            fileName = file.name,
            filePath = file.absolutePath,
            fileUri = exportedUri,
            fileSizeFormatted = sizeFormatted,
            durationFormatted = durationFormatted,
            sampleRateHz = sampleRate,
            channels = channels,
            bitDepth = bitDepth,
            bitrateKbps = bitrateKbps,
            formatDescription = "Lossless WAV (16-bit PCM Linear)",
            createdTimeFormatted = timeFormatted,
            appliedToneTitle = toneTitle,
            appliedModulationTitle = modulationTitle,
            appliedStyleTitle = styleTitle,
            finalPitch = pitch,
            finalSpeed = speed,
            appliedDepth = depth,
            appliedEmotion = emotion,
            sourceTextSnippet = textSnippet.take(60) + if (textSnippet.length > 60) "..." else ""
        )
    }

    /**
     * Plays back exported audio file using Android MediaPlayer
     */
    fun playAudioFile(file: File, onFinished: () -> Unit) {
        stopExportedPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    _isPlayingExported.value = false
                    onFinished()
                }
                start()
            }
            _isPlayingExported.value = true
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer error: ${e.message}")
            _isPlayingExported.value = false
        }
    }

    /**
     * Plays back bundled ASMR master track directly from app assets
     */
    fun playAssetAudio(assetPath: String, onFinished: () -> Unit) {
        stopExportedPlayback()
        stop()
        try {
            val afd = context.assets.openFd(assetPath)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                setOnCompletionListener {
                    _isPlayingExported.value = false
                    onFinished()
                }
                start()
            }
            _isPlayingExported.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Asset MediaPlayer error: ${e.message}")
            _isPlayingExported.value = false
        }
    }

    fun stopExportedPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaPlayer = null
            _isPlayingExported.value = false
        }
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
    }

    /**
     * Inserts natural prosodic pauses or breath phrasing for special styles
     */
    private fun formatTamilSpeechText(text: String, isWhisper: Boolean, isRobotic: Boolean): String {
        return when {
            isRobotic -> {
                // Monotone spacing between phrases
                text.replace(",", " .").replace(";", " .")
            }
            isWhisper -> {
                // Soft phrasing with gentle pauses
                text.replace(".", "... ").replace(",", ".. ")
            }
            else -> text
        }
    }

    /**
     * Ensures valid RIFF WAVE header exists on file
     */
    private fun ensureValidWavHeader(file: File) {
        try {
            if (!file.exists() || file.length() < 4) return
            val header = ByteArray(4)
            FileInputStream(file).use { it.read(header) }
            if (String(header) != "RIFF") {
                // Raw PCM synthesized, wrap into WAV container
                val pcmData = file.readBytes()
                val sampleRate = 22050
                val channels = 1
                val bitDepth = 16
                val wavBytes = createWavBytes(pcmData, sampleRate, channels, bitDepth)
                FileOutputStream(file).use { it.write(wavBytes) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Header check exception: ${e.message}")
        }
    }

    private fun createWavBytes(pcmData: ByteArray, sampleRate: Int, channels: Int, bitDepth: Int): ByteArray {
        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * channels * bitDepth / 8
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0 // PCM format
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitDepth / 8).toByte(); header[33] = 0
        header[34] = bitDepth.toByte(); header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmData.size and 0xff).toByte()
        header[41] = (pcmData.size shr 8 and 0xff).toByte()
        header[42] = (pcmData.size shr 16 and 0xff).toByte()
        header[43] = (pcmData.size shr 24 and 0xff).toByte()

        return header + pcmData
    }
}
