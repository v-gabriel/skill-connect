package hr.vgabriel.skillconnect.bll.services

import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


enum class ToastType {
    INFO, ERROR
}

interface IToastService {
    fun setToast(toastData: ToastData)
}

val LocalToastService = staticCompositionLocalOf<ToastService> { error("No ToastService provided") }

data class ToastData(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val closeable: Boolean = true
)

class ToastService() : IToastService {

    private val _toastData: MutableStateFlow<ToastData?> = MutableStateFlow(null)
    val toastData: StateFlow<ToastData?> = _toastData.asStateFlow()

    private val _showToast: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showToast: StateFlow<Boolean> = _showToast.asStateFlow()

    override fun setToast(toastData: ToastData) {
        _toastData.value = toastData;
        _showToast.value = !_showToast.value
    }

    @Composable
    fun getBackgroundColor(type: ToastType): Color {
        return when (type) {
            ToastType.INFO -> MaterialTheme.colors.primary
            ToastType.ERROR -> MaterialTheme.colors.error
        }
    }

    @Composable
    fun getContentColor(type: ToastType): Color {
        return when (type) {
            ToastType.INFO -> MaterialTheme.colors.onPrimary
            ToastType.ERROR -> MaterialTheme.colors.onError
        }
    }
}
