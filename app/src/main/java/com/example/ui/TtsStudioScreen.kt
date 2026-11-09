package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsInitState
import com.example.ui.components.AudioDetailsDialog
import com.example.ui.components.FinalVoiceSection
import com.example.ui.components.ModulationSection
import com.example.ui.components.PitchSpeedSection
import com.example.ui.components.TamilTextInputSection
import com.example.ui.components.ToneSelectorSection
import com.example.ui.components.VoiceStyleSection
import com.example.ui.theme.StudioCoralTertiary
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurplePrimary
import com.example.ui.theme.StudioRoseSecondary
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsStudioScreen(viewModel: TtsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val initState by viewModel.initState.collectAsState()
    val formulaPair by viewModel.calculatedFormula.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.exportSuccessMessage) {
        uiState.exportSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Subiksha Teacher App Logo",
                                tint = StudioPurplePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "சுபிக்ஷா டீச்சர் செயலி",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Tamil Female TTS Voice Studio",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = StudioRoseSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Engine Status indicator
                    val isReady = initState is TtsInitState.Ready
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isReady) SuccessGreen.copy(alpha = 0.15f)
                                else StudioCoralTertiary.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isReady) SuccessGreen else StudioCoralTertiary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isReady) "தமிழ் TTS தயார்" else "இணைக்கப்படுகிறது",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isReady) SuccessGreen else StudioCoralTertiary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Studio vs Saved Audio Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("குரல் ஸ்டுடியோ (Studio)", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_studio")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("பதிவிறக்கியவை (${uiState.savedHistory.size})", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_saved_audios")
                )
            }

            if (selectedTab == 0) {
                // Main Studio Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("studio_lazy_column"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Text Input Section
                    item {
                        TamilTextInputSection(
                            text = uiState.inputText,
                            onTextChanged = viewModel::onTextChanged
                        )
                    }

                    // 2. Section 5: Final Output Voice & Quick Player (Placed on top for fast access as well)
                    item {
                        FinalVoiceSection(
                            selectedTone = uiState.selectedTone,
                            selectedModulation = uiState.selectedModulation,
                            selectedStyle = uiState.selectedStyle,
                            effectivePitch = formulaPair.first,
                            effectiveSpeed = formulaPair.second,
                            isSpeaking = isSpeaking,
                            isExporting = uiState.isExporting,
                            hasLastExportedAudio = uiState.currentInspectedAudio != null,
                            onSpeakClick = viewModel::speakFinalOutput,
                            onStopClick = viewModel::stopSpeaking,
                            onExportClick = viewModel::exportLosslessWav,
                            onViewDetailsClick = {
                                uiState.currentInspectedAudio?.let { viewModel.showAudioDetails(it) }
                            }
                        )
                    }

                    // 3. Section 1: Tone Selection (15 Female Tones)
                    item {
                        ToneSelectorSection(
                            selectedTone = uiState.selectedTone,
                            onToneSelected = viewModel::onToneSelected,
                            onPreviewTone = viewModel::previewTone,
                            previewingItemName = uiState.previewingItemName
                        )
                    }

                    // 4. Section 2: Voice Modulation (10 Modulations)
                    item {
                        ModulationSection(
                            selectedModulation = uiState.selectedModulation,
                            onModulationSelected = viewModel::onModulationSelected,
                            onPreviewModulation = viewModel::previewModulation,
                            previewingItemName = uiState.previewingItemName
                        )
                    }

                    // 5. Section 3: Special Female Voice Styles (14 Styles)
                    item {
                        VoiceStyleSection(
                            selectedStyle = uiState.selectedStyle,
                            onStyleSelected = viewModel::onStyleSelected,
                            onPreviewStyle = viewModel::previewStyle,
                            previewingItemName = uiState.previewingItemName
                        )
                    }

                    // 6. Section 4: Pitch & Speed Adjustments + Depth & Emotion
                    item {
                        PitchSpeedSection(
                            pitchPercent = uiState.pitchPercent,
                            onPitchChanged = viewModel::onPitchChanged,
                            speedMultiplier = uiState.speedMultiplier,
                            onSpeedChanged = viewModel::onSpeedChanged,
                            voiceDepth = uiState.voiceDepth,
                            onDepthSelected = viewModel::onDepthSelected,
                            voiceEmotion = uiState.voiceEmotion,
                            onEmotionSelected = viewModel::onEmotionSelected
                        )
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // Saved Audios History Tab
                if (uiState.savedHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "இன்னும் ஆடியோக்கள் பதிவிறக்கப்படவில்லை",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "குரல் ஸ்டுடியோவில் WAV பதிவிறக்கு பொத்தானை அழுத்தி தரம் குறையாத ஆடியோவை சேமிக்கவும்.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.savedHistory) { audio ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = audio.fileName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = audio.fileSizeFormatted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SuccessGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${audio.appliedToneTitle} • ${audio.appliedStyleTitle}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = audio.sourceTextSnippet,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.showAudioDetails(audio) },
                                            modifier = Modifier.testTag("saved_item_details_${audio.fileName}")
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = "Details", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("விவரங்கள்", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Details Modal Dialog
        if (uiState.showDetailsDialog && uiState.currentInspectedAudio != null) {
            AudioDetailsDialog(
                info = uiState.currentInspectedAudio!!,
                isPlaying = uiState.isPlayingExported,
                onPlay = { viewModel.playInspectedAudio(uiState.currentInspectedAudio!!) },
                onStop = viewModel::stopInspectedAudio,
                onDismiss = viewModel::dismissDetailsDialog
            )
        }
    }
}
