package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.VoiceDepth
import com.example.model.VoiceEmotion
import com.example.model.VoicePresetsData
import com.example.ui.TtsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Subiksha TTS", appName)
  }

  @Test
  fun `verify female tone presets count`() {
    assertTrue(VoicePresetsData.femaleTones.size >= 15)
    assertTrue(VoicePresetsData.voiceModulations.size >= 10)
    assertTrue(VoicePresetsData.specialVoiceStyles.size >= 14)
    assertTrue(VoicePresetsData.characterVoices.isNotEmpty())
    assertTrue(VoicePresetsData.characterVoices.any { it.isAsmr })
  }

  @Test
  fun `verify studio formula calculation`() {
    val tone = VoicePresetsData.femaleTones.first()
    val mod = VoicePresetsData.voiceModulations.first()
    val style = VoicePresetsData.specialVoiceStyles.first()

    val (pitch, speed) = TtsViewModel.calculateCombinedVoiceOutput(
      tone = tone,
      modulation = mod,
      style = style,
      pitchPercent = 78,
      speedMultiplier = 1.0f,
      depth = VoiceDepth.MEDIUM,
      emotion = VoiceEmotion.NORMAL
    )

    assertTrue(pitch in 0.5f..2.0f)
    assertTrue(speed in 0.25f..3.0f)
  }
}
