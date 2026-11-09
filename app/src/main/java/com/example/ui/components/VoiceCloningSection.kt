package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClonedVoiceProfile
import com.example.model.VoiceClonePresets
import com.example.model.VoiceCloningMode
import com.example.ui.theme.StudioCoralTertiary
import com.example.ui.theme.AccentGold
import com.example.ui.theme.StudioPurplePrimary
import com.example.ui.theme.StudioRoseSecondary
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceCloningSection(
    inputText: String,
    onTextChanged: (String) -> Unit,
    selectedProfile: ClonedVoiceProfile,
    onProfileSelected: (ClonedVoiceProfile) -> Unit,
    selectedMode: VoiceCloningMode,
    onModeSelected: (VoiceCloningMode) -> Unit,
    whisperBoostPercent: Int,
    onWhisperBoostChanged: (Int) -> Unit,
    elevenApiKey: String,
    onApiKeyChanged: (String) -> Unit,
    isGenerating: Boolean,
    isPlaying: Boolean,
    statusMessage: String,
    onCloneAndSpeak: () -> Unit,
    onStopSpeaking: () -> Unit,
    onExportWav: () -> Unit,
    onPlayMasterAsset: (String) -> Unit
) {
    var showApiKeyField by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_cloning_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Cloned Voice Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = borderGradient()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(StudioRoseSecondary, StudioPurplePrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedProfile.avatarEmoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedProfile.nameTa,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SuccessGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "100% Cloned",
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                text = selectedProfile.nameEn,
                                fontSize = 12.sp,
                                color = StudioRoseSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Button to hear the original master audio directly
                    OutlinedButton(
                        onClick = { onPlayMasterAsset(selectedProfile.assetAudioPath) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("play_original_master_audio_btn")
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp), tint = StudioPurplePrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("அசல் ஆடியோ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = selectedProfile.descriptionTa,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Profile Selector chips (Bae vs Bae Comfort)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoiceClonePresets.profiles.forEach { profile ->
                        val isSelected = profile.id == selectedProfile.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onProfileSelected(profile) },
                            label = {
                                Text(
                                    text = "${profile.avatarEmoji} ${profile.nameTa.substringBefore(" (")}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioPurplePrimary.copy(alpha = 0.18f),
                                selectedLabelColor = StudioPurplePrimary
                            )
                        )
                    }
                }
            }
        }

        // 2. Engine Mode Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "குரல் குளோனிங் தொழில்நுட்பம் (Cloning Engine Mode)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoiceCloningMode.values().forEach { mode ->
                        val isSelected = mode == selectedMode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onModeSelected(mode) },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = mode.titleTa,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = mode.subtitleEn,
                                        fontSize = 9.sp,
                                        color = if (isSelected) StudioPurplePrimary else Color.Gray
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioPurplePrimary.copy(alpha = 0.15f),
                                selectedLabelColor = StudioPurplePrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // If IndicF5 mode selected, show IndicF5 IIT Madras details
                AnimatedVisibility(visible = selectedMode == VoiceCloningMode.INDIC_F5) {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudioPurplePrimary.copy(alpha = 0.08f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioPurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "IIT Madras AI4Bharat IndicF5: உங்கள் Bae அசல் ஆடியோவிலிருந்து 100% Zero-Shot முறையில் தமிழ் உச்சரிப்புடன் குரல் குளோனிங் செய்யப்படுகிறது. கட்டணம் இல்லை, நேரடி AI குரல்.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // If ElevenLabs mode selected, show API key expander
                AnimatedVisibility(visible = selectedMode == VoiceCloningMode.CLOUD_ELEVENLABS) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showApiKeyField = !showApiKeyField }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ElevenLabs API Key உள்ளிடுங்கள்", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                            }
                            Icon(
                                if (showApiKeyField) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AccentGold
                            )
                        }

                        if (showApiKeyField || elevenApiKey.isBlank()) {
                            OutlinedTextField(
                                value = elevenApiKey,
                                onValueChange = onApiKeyChanged,
                                placeholder = { Text("xi-api-key உள்ளிடவும் (e.g. sk_...)", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Text Input Box with Tanglish Sample Chips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "பேச வேண்டிய வாக்கியம் (தமிழ் / Tanglish)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "இயற்கையான உச்சரிப்பு",
                        fontSize = 11.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cloned_voice_text_input"),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            text = "எடுத்துக்காட்டு: Hi hello enna panra? அல்லது டார்லிங் கோவப்படாதடா..",
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPurplePrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fast sample suggestion chips
                Text(text = "விரைவு மாதிரி வாக்கியங்கள்:", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedProfile.samplePhrases.forEach { sample ->
                        SuggestionChip(
                            onClick = { onTextChanged(sample) },
                            label = { Text(sample.take(24) + "...", fontSize = 11.sp) }
                        )
                    }
                    SuggestionChip(
                        onClick = { onTextChanged("Hi hello enna panra? சாப்பிட்டியா?") },
                        label = { Text("Hi hello enna panra?", fontSize = 11.sp, color = StudioRoseSecondary) }
                    )
                }
            }
        }

        // 4. Acoustic Whisper Boost slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "விஸ்பர் மென்மை & மூச்சு ஒலி (Whisper & Breath)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$whisperBoostPercent%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = StudioRoseSecondary
                    )
                }

                Slider(
                    value = whisperBoostPercent.toFloat(),
                    onValueChange = { onWhisperBoostChanged(it.toInt()) },
                    valueRange = 50f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = StudioRoseSecondary,
                        activeTrackColor = StudioRoseSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "அசல் ஆடியோவின் அதே 78% Pitch மற்றும் மென்மையான விஸ்பர் அலைவரிசை பூட்டப்பட்டுள்ளது.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // 5. Visualizer & Status Display
        if (isGenerating || isPlaying) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StudioPurplePrimary.copy(alpha = 0.08f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = StudioPurplePrimary,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        // Animated wave visualizer
                        WaveformPulseBar(isPlaying = isPlaying)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusMessage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPurplePrimary
                    )
                }
            }
        }

        // 6. Action Control Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main "Clone & Speak" Button
            Button(
                onClick = {
                    if (isPlaying) onStopSpeaking() else onCloneAndSpeak()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("clone_and_speak_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) StudioCoralTertiary else StudioPurplePrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "நிறுத்து (Stop Voice)" else "▶ Cloned குரலில் பேசு (Clone & Speak)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Export to Lossless WAV
            OutlinedButton(
                onClick = onExportWav,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("export_cloned_wav_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = StudioRoseSecondary
                )
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "இந்த Cloned Voice-ஐ Lossless WAV ஆக சேமி",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun WaveformPulseBar(isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "wave_pulse")
    val heights = (0..9).map { index ->
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 300 + (index * 80), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEachIndexed { idx, animValue ->
            val scale = if (isPlaying) animValue.value else 0.2f
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .width(4.dp)
                    .height((32 * scale).coerceAtLeast(4f).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (idx % 2 == 0) StudioPurplePrimary else StudioRoseSecondary
                    )
            )
        }
    }
}

@Composable
private fun borderGradient() = androidx.compose.foundation.BorderStroke(
    width = 1.5.dp,
    brush = Brush.linearGradient(
        colors = listOf(StudioRoseSecondary.copy(alpha = 0.6f), StudioPurplePrimary.copy(alpha = 0.3f))
    )
)
