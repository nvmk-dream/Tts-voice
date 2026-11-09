package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AudioMetadataInfo
import com.example.ui.theme.StudioCoralTertiary
import com.example.ui.theme.StudioPurplePrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun AudioDetailsDialog(
    info: AudioMetadataInfo,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("audio_details_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = "Audio Specs",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ஆடியோ விவரங்கள்",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Audio Details Inspector",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_details_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Download Status Badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessGreen.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = "Saved",
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "தரம் குறையாமல் பதிவிறக்கப்பட்டது",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SuccessGreen
                            )
                            Text(
                                text = "இடம்: Downloads / Subiksha_TTS_Audio /",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Audio Hardware Specs
                Text(
                    text = "தொழில்நுட்ப விவரங்கள் (Audio Specifications)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                SpecItemRow(label = "கோப்பு பெயர் (File Name)", value = info.fileName)
                SpecItemRow(label = "ஒலி வடிவம் (Format)", value = info.formatDescription)
                SpecItemRow(label = "மாதிரி வீதம் (Sample Rate)", value = "${info.sampleRateHz} Hz (${info.sampleRateHz / 1000.0} kHz)")
                SpecItemRow(label = "சேனல் (Channels)", value = if (info.channels == 1) "1 (Mono Speech)" else "2 (Stereo)")
                SpecItemRow(label = "பிட் தரம் (Bit Depth)", value = "${info.bitDepth}-bit Linear PCM")
                SpecItemRow(label = "பிட்ரேட் (Bitrate)", value = "${info.bitrateKbps} kbps (Lossless High Fidelity)")
                SpecItemRow(label = "கால அளவு (Duration)", value = info.durationFormatted)
                SpecItemRow(label = "கோப்பு அளவு (File Size)", value = info.fileSizeFormatted)
                SpecItemRow(label = "உருவாக்கப்பட்ட நேரம் (Created)", value = info.createdTimeFormatted)

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Studio Tone Settings Used
                Text(
                    text = "பயன்படுத்தப்பட்ட Voice Settings",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                SpecItemRow(label = "1. Tone", value = info.appliedToneTitle)
                SpecItemRow(label = "2. Modulation", value = info.appliedModulationTitle)
                SpecItemRow(label = "3. Voice Style", value = info.appliedStyleTitle)
                SpecItemRow(label = "4. Final Pitch (சுருதி)", value = String.format("%.2fx", info.finalPitch))
                SpecItemRow(label = "5. Final Speed (வேகம்)", value = String.format("%.2fx", info.finalSpeed))
                SpecItemRow(label = "Voice Depth", value = info.appliedDepth)
                SpecItemRow(label = "Voice Emotion", value = info.appliedEmotion)

                Spacer(modifier = Modifier.height(16.dp))

                // Audio Playback & Share Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isPlaying) {
                        Button(
                            onClick = onStop,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCoralTertiary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stop_inspected_audio_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("நிறுத்து")
                        }
                    } else {
                        Button(
                            onClick = onPlay,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPurplePrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("play_inspected_audio_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("கேட்டுப்பார்")
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "audio/wav"
                                if (info.fileUri != null) {
                                    putExtra(Intent.EXTRA_STREAM, info.fileUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                putExtra(Intent.EXTRA_SUBJECT, "Subiksha Tamil Female Voice Audio")
                                putExtra(Intent.EXTRA_TEXT, "சுபிக்ஷா டீச்சர் செயலி மூலம் உருவாக்கப்பட்ட தமிழ் பெண் குரல் ஆடியோ: ${info.sourceTextSnippet}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "ஆடியோவை பகிர்"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_audio_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("பகிர்")
                    }
                }
            }
        }
    }
}

@Composable
fun SpecItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.3f)
        )
    }
}
