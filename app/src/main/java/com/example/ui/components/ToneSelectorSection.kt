package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FemaleTone
import com.example.model.VoicePresetsData

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToneSelectorSection(
    selectedTone: FemaleTone,
    onToneSelected: (FemaleTone) -> Unit,
    onPreviewTone: (FemaleTone) -> Unit,
    previewingItemName: String?
) {
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "General", "Professional", "Emotional", "Storytelling", "Youth")

    val filteredTones = remember(selectedCategory) {
        if (selectedCategory == "All") {
            VoicePresetsData.femaleTones
        } else {
            VoicePresetsData.femaleTones.filter { it.category == selectedCategory }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
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
                        text = "1",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "1. Tone Selection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "குரலின் அடிப்படைத் தன்மையைத் தேர்வு செய்யவும்",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = when (cat) {
                                "All" -> "அனைத்தும் (${VoicePresetsData.femaleTones.size})"
                                "General" -> "பொதுவானது"
                                "Professional" -> "தொழில்முறை"
                                "Emotional" -> "உணர்ச்சி"
                                "Storytelling" -> "கதை நயம்"
                                "Youth" -> "இளமை/மழலை"
                                else -> cat
                            },
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.testTag("tone_cat_$cat")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cards list
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredTones.forEach { tone ->
                val isCurrentSelected = tone.id == selectedTone.id
                val isPreviewingThis = previewingItemName == tone.titleTa

                val containerColor by animateColorAsState(
                    targetValue = if (isCurrentSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    label = "tone_bg"
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = BorderStroke(
                        width = if (isCurrentSelected) 1.8.dp else 1.dp,
                        color = if (isCurrentSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToneSelected(tone) }
                        .testTag("tone_card_${tone.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isCurrentSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tone.titleEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isCurrentSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                                Text(
                                    text = tone.titleTa,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = tone.descriptionTa,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Dedicated Preview Voice Button
                        OutlinedButton(
                            onClick = {
                                onToneSelected(tone)
                                onPreviewTone(tone)
                            },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isCurrentSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.testTag("preview_tone_${tone.id}")
                        ) {
                            Icon(
                                imageVector = if (isPreviewingThis) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                                contentDescription = "Preview",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Preview",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
