package hr.vgabriel.skillconnect.ui.components

import Screen
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import hr.vgabriel.skillconnect.R
import hr.vgabriel.skillconnect.bll.services.LocalAuthService
import hr.vgabriel.skillconnect.bll.services.LocalLoggingService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LogType
import hr.vgabriel.skillconnect.definitions.vm.LocalChatThreadsVM
import kotlinx.coroutines.launch

@Composable
fun Init() {
    val authService = LocalAuthService.current
    val chatThreadsVM = LocalChatThreadsVM.current
    val loggingService = LocalLoggingService.current
    val navService = LocalNavService.current

    LaunchedEffect(Unit) {
        val initSuccess = authService.initUser()

        if (initSuccess) {
            chatThreadsVM.initChatEventServiceHandlers()

            navService.navigateToScreen(Screen.Chats)
            chatThreadsVM.viewModelScope.launch{
                chatThreadsVM.initChatThreadItemVMs()
            }
        } else {
            navService.navigateToScreen(Screen.Landing)
        }
        navService.resetPreviousScreens()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Skill Connect Logo",
            modifier = Modifier
                .size(75.dp)
                .offset(y = (-32).dp)
        )
    }
}