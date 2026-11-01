package dev.arnv.bluke.ui

import android.content.SharedPreferences
import androidx.core.content.edit

private const val INPUT_MODE_CYCLE_PREFERENCE = "cycle_connection_modes"
private const val INPUT_MODE_CYCLE_MIGRATED_PREFERENCE = "input_mode_cycle_migrated"

enum class InputMode(
    val id: Int,
    val preferenceKey: String,
    val displayName: String,
) {
    KEYBOARD(0, "keyboard", "Keyboard"),
    TOUCHPAD(1, "touchpad", "Touchpad"),
    GAMEPAD(2, "gamepad", "Gamepad"),
    MEDIA_PRESENTATION(4, "media_presentation", "Multimedia"),
    ;

    companion object {
        fun fromId(id: Int): InputMode = entries.firstOrNull { it.id == id } ?: KEYBOARD
    }
}

private val legacyDefaultModeKeys = setOf("keyboard", "touchpad", "gamepad")
private const val REMOVED_MOUSE_MODE_KEY = "mouse"
private const val REMOVED_KEYBOARD_TOUCHPAD_MODE_KEY = "keyboard_touchpad"
val defaultInputModeKeys: Set<String> = InputMode.entries.mapTo(linkedSetOf()) { it.preferenceKey }

fun normalizeInputModeKeys(savedKeys: Set<String>?, legacyDefaultMigrated: Boolean = false): Set<String> {
    val normalized = when {
        savedKeys == null -> defaultInputModeKeys
        savedKeys == legacyDefaultModeKeys && !legacyDefaultMigrated -> defaultInputModeKeys
        else -> savedKeys.mapNotNullTo(linkedSetOf()) { key ->
            when (key) {
                REMOVED_MOUSE_MODE_KEY -> InputMode.MEDIA_PRESENTATION.preferenceKey
                REMOVED_KEYBOARD_TOUCHPAD_MODE_KEY -> null
                else -> key
            }
        }.filterTo(linkedSetOf()) { key ->
            InputMode.entries.any { it.preferenceKey == key }
        }
    }
    return normalized.ifEmpty { setOf(InputMode.KEYBOARD.preferenceKey) }
}

fun SharedPreferences.enabledInputModes(): List<InputMode> {
    val saved = getStringSet(INPUT_MODE_CYCLE_PREFERENCE, null)?.toSet()
    // The old default only migrates once. After that, the same set is a deliberate choice.
    val migrated = getBoolean(INPUT_MODE_CYCLE_MIGRATED_PREFERENCE, false)
    val normalized = normalizeInputModeKeys(saved, legacyDefaultMigrated = migrated)
    if (saved != normalized || !migrated) {
        edit {
            putStringSet(INPUT_MODE_CYCLE_PREFERENCE, normalized)
            putBoolean(INPUT_MODE_CYCLE_MIGRATED_PREFERENCE, true)
        }
    }
    return InputMode.entries.filter { normalized.contains(it.preferenceKey) }
}
