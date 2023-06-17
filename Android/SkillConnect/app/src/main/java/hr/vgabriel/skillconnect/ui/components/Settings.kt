package hr.vgabriel.skillconnect.ui.components

import Screen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hr.vgabriel.skillconnect.bll.providers.LocalUserProvider
import hr.vgabriel.skillconnect.bll.services.DialogData
import hr.vgabriel.skillconnect.bll.services.DialogType
import hr.vgabriel.skillconnect.bll.services.LocalAuthService
import hr.vgabriel.skillconnect.bll.services.LocalDialogService
import hr.vgabriel.skillconnect.bll.services.LocalLoadingService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LocalToastService
import hr.vgabriel.skillconnect.bll.services.ToastData

@Composable
fun Settings(
) {
    val userProvider = LocalUserProvider.current
    val dialogService = LocalDialogService.current
    val authService = LocalAuthService.current
    val loadingService = LocalLoadingService.current
    val toastService = LocalToastService.current
    val navService = LocalNavService.current

    val performLogout = remember { mutableStateOf(false) }

    LaunchedEffect(performLogout.value) {
        if (performLogout.value) {
            loadingService.delegateLoad {
                try {
                    val isLogout = authService.logout()

                    if (isLogout) {
                        toastService.setToast(
                            ToastData(
                                message = "Logout successful!"
                            )
                        )
                        navService.navigateToScreen(Screen.Init)
                    } else {
                        throw Exception("Logout error")
                    }
                } catch (e: Exception) {
                    toastService.setToast(
                        ToastData(
                            message = "Something went wrong. Please try again later."
                        )
                    )
                }
            }
        }
    }

    Column() {
        SettingItem(
            icon = Icons.Default.Person,
            title = userProvider.user!!.fullName,
            description = "Edit personal information",
            onClick = {
                navService.navigateToScreen(Screen.Profile)
            },
            showSuffixIcon = true
        )

        SettingItem(
            icon = Icons.Default.Logout,
            title = "Logout",
            onClick = {
                dialogService.setDialog(
                    DialogData(
                        message = "Are you sure you want to logout?",
                        type = DialogType.INFO,
                        onConfirm = {
                            performLogout.value = true
                        },
                    )
                )
            }
        )
    }
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    showSuffixIcon: Boolean = false,
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Setting Icon",
            modifier = Modifier.padding(end = 16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )

            if (description != null) {
                Text(
                    text = description,
                    style = TextStyle(color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f))
                )
            }
        }

        if(showSuffixIcon){
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate Icon",
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}