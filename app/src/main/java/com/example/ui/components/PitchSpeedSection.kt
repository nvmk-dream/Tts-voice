package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VoiceDepth
import com.example.model.VoiceEmotion
import java.util.Locale

@Composable
fun PitchSpeedSection(
    pitchPercent: Int,
    onPitchChanged: (Int) -> Unit,
    speedMultiplier: Float,
    onSpeedChanged: (Float) -> Unit,
    voiceDepth: VoiceDepth,
    onDepthSelected: (VoiceDepth) -> Unit,
    voiceEmotion: VoiceEmotion,
    onEmotionSelected: (VoiceEmotion) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "4",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "4. Pitch & Speed Adjustment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "சுருதி, வேகம் மற்றும் குரலின் ஆழத்தை மாற்றியமைக்கவும்",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Pitch Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Pitch",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pitch (சுருதி)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$pitchPercent%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Slider(
                value = pitchPercent.toFloat(),
                onValueChange = { onPitchChanged(it.toInt()) },
                valueRange = 20f..150f,
                steps = 25,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pitch_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Low (20%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Medium (78%)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text("High (150%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Speed Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Speed (பேச்சு வேகம்)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.2fx", speedMultiplier),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Slider(
                value = speedMultiplier,
                onValueChange = { onSpeedChanged(it) },
                valueRange = 0.5f..2.5f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("speed_slider")
            )

            // Quick Speed Preset Chips (0.75x, 1.0x, 1.5x, 2.0x)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.75f, 1.00f, 1.25f, 1.50f, 2.00f).forEach { preset ->
                    val isSelected = Math.abs(speedMultiplier - preset) < 0.05f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSpeedChanged(preset) },
                        label = { Text("${preset}x", fontSize = 11.sp) },
                        modifier = Modifier.testTag("speed_preset_${preset}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Voice Depth (Light, Medium, Deep)
            Text(
                text = "Voice Depth (குரல் கனம் / ஆழம்)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceDepth.values().forEach { depth ->
                    val isSelected = depth == voiceDepth
                    FilterChip(
                        selected = isSelected,
                        onClick = { onDepthSelected(depth) },
                        label = { Text(depth.titleTa, fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("depth_chip_${depth.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Voice Emotion (Soft, Normal, Expressive)
            Text(
                text = "Emotion (உணர்ச்சி அளவு)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceEmotion.values().forEach { emotion ->
                    val isSelected = emotion == voiceEmotion
                    FilterChip(
                        selected = isSelected,
                        onClick = { onEmotionSelected(emotion) },
                        label = { Text(emotion.titleTa, fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("emotion_chip_${emotion.name}")
                    )
                }
            }
        }
    }
}
