package dev.arnv.bluke.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InputModePreferencesTest {
    private val cycleKey = "cycle_connection_modes"
    private val withoutMultimedia = listOf(InputMode.KEYBOARD, InputMode.TOUCHPAD, InputMode.GAMEPAD)
    private val preferences by lazy {
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("input-mode-test", Context.MODE_PRIVATE)
    }

    private fun saveCycle(modes: List<InputMode>) {
        preferences.edit().putStringSet(cycleKey, modes.mapTo(mutableSetOf()) { it.preferenceKey }).commit()
    }

    @Before
    fun clearPreferences() {
        preferences.edit().clear().commit()
    }

    @Test
    fun freshInstallEnablesEveryMode() {
        assertEquals(InputMode.entries, preferences.enabledInputModes())
    }

    @Test
    fun oldDefaultGainsMultimediaOnUpgrade() {
        saveCycle(withoutMultimedia)

        assertEquals(InputMode.entries, preferences.enabledInputModes())
    }

    @Test
    fun turningOffOnlyMultimediaSticksAfterMigration() {
        preferences.enabledInputModes()
        saveCycle(withoutMultimedia)

        assertEquals(withoutMultimedia, preferences.enabledInputModes())
        assertEquals(withoutMultimedia, preferences.enabledInputModes())
    }

    @Test
    fun deliberateSubsetSurvivesTheMigration() {
        saveCycle(listOf(InputMode.TOUCHPAD))

        assertEquals(listOf(InputMode.TOUCHPAD), preferences.enabledInputModes())
    }
}
