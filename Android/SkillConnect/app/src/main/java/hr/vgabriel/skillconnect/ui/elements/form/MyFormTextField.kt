package hr.vgabriel.skillconnect.ui.elements.form

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.vgabriel.skillconnect.ui.elements.shared.MyTextField

@Composable
fun MyFormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String = "",
    infoMessage: String = "",
    requestFocusInitially: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    onSubmit: () -> Unit = {},
    enabled: Boolean = true,
    buttonIcon: ImageVector? = null,
    onButtonClick: () -> Unit = {}
) {
    val focusRequester = remember {
        FocusRequester.Default
    }

    val initialFocusPassed = remember {
        mutableStateOf(false)
    }
    val isFocused = remember { mutableStateOf(requestFocusInitially) }
    val hasBeenUnfocused = remember { mutableStateOf(false) }

    val submitKeyPressed = remember { mutableStateOf(false) }
    val shouldShowError = remember(isFocused, hasBeenUnfocused, submitKeyPressed, isError) {
        derivedStateOf {
            (hasBeenUnfocused.value || submitKeyPressed.value) && isError && initialFocusPassed.value
        }
    }
    Column(modifier) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colors.surface, ShapeDefaults.Small)
                .border(
                    border = BorderStroke(0.dp, Color.Transparent),
                    shape = ShapeDefaults.Small
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyTextField(
                value = value,
                onValueChange = { inputValue ->
                    onValueChange(inputValue)
                },
                label = { Text(label) },
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .weight(1f)
                    .onFocusChanged {
                        isFocused.value = it.isFocused
                        if (initialFocusPassed.value && !it.isFocused) {
                            hasBeenUnfocused.value = true
                        }
                        initialFocusPassed.value = true
                    }
                    .onPreviewKeyEvent { event ->
                        if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                            submitKeyPressed.value = true
                            onSubmit()
                        }
                        false
                    },
                enabled = enabled,
            )

            if (buttonIcon != null) {
                IconButton(
                    onClick = {
                        onButtonClick()
                    }, modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = buttonIcon,
                        contentDescription = "Button",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }


        if (infoMessage.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = infoMessage,
                color = MaterialTheme.colors.primary.copy(alpha = 0.75f),
                style = MaterialTheme.typography.body2.copy(fontSize = 12.sp)
            )
        }

        if (shouldShowError.value) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.body2.copy(fontSize = 12.sp)
            )
        }
    }
}








