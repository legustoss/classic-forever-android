package app.gamenative.utils

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.winlator.inputcontrols.ControllerManager

/** Observes gamepad connection for launcher UI only. Never calls ControllerManager's game-side connect hooks. */
@Composable
fun rememberGamepadConnected(): State<Boolean> {
    val context = LocalContext.current
    val connected = remember { mutableStateOf(hasGamepad()) }

    DisposableEffect(context) {
        val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
        val listener = object : InputManager.InputDeviceListener {
            override fun onInputDeviceAdded(deviceId: Int) { connected.value = hasGamepad() }
            override fun onInputDeviceRemoved(deviceId: Int) { connected.value = hasGamepad() }
            override fun onInputDeviceChanged(deviceId: Int) { connected.value = hasGamepad() }
        }
        connected.value = hasGamepad()
        inputManager.registerInputDeviceListener(listener, null)
        onDispose { inputManager.unregisterInputDeviceListener(listener) }
    }

    return connected
}

private fun hasGamepad(): Boolean =
    InputDevice.getDeviceIds().any { id ->
        InputDevice.getDevice(id)?.let { ControllerManager.isGameController(it) } == true
    }

/** Compose already handles DPAD_CENTER/ENTER; only BUTTON_A needs explicit mapping. */
fun isLauncherGamepadConfirm(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_BUTTON_A

fun isLauncherGamepadBack(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_BUTTON_B
