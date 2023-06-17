package hr.vgabriel.skillconnect.bll.services

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import hr.vgabriel.skillconnect.ui.elements.shared.MyOutlinedButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DialogType {
    INFO, ERROR
}

interface IDialogService {
    fun setDialog(dialogData: DialogData)

    @Composable
    fun DialogInit()
}

val LocalDialogService =
    staticCompositionLocalOf<DialogService> { error("No DialogService provided") }

data class DialogData(
    val message: String,
    val type: DialogType? = null,
    val onConfirm: () -> Unit = {},
    val onCancel: () -> Unit = {}
)

class DialogService(

) : IDialogService {

    private val _dialogData: MutableStateFlow<DialogData?> = MutableStateFlow(null)
    val dialogData: StateFlow<DialogData?> = _dialogData.asStateFlow()

    private val _showDialog: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    override fun setDialog(dialogData: DialogData) {
        _dialogData.value = dialogData;
        _showDialog.value = !_showDialog.value
    }

    @Composable
    override fun DialogInit() {
        val showDialog = showDialog.collectAsState()
        val dialogData = dialogData.collectAsState()

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    _showDialog.value = false
                    _dialogData.value?.onCancel?.invoke()
                },
                text = {
                    Text(text = "${dialogData.value?.message}")
                },
                confirmButton = {
                    MyOutlinedButton(
                        onClick = {
                            _showDialog.value = false
                            _dialogData.value?.onConfirm?.invoke()
                        }
                    ) {
                        Text("Ok")
                    }
                },
                dismissButton = {
                    MyOutlinedButton(
                        onClick = {
                            _showDialog.value = false
                            _dialogData.value?.onCancel?.invoke()
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}