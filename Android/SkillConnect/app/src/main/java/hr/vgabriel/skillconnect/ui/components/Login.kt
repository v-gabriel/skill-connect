package hr.vgabriel.skillconnect.ui.components

import Screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import hr.vgabriel.skillconnect.bll.services.LocalAuthService
import hr.vgabriel.skillconnect.bll.services.LocalLoadingService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LocalToastService
import hr.vgabriel.skillconnect.bll.services.ToastData
import hr.vgabriel.skillconnect.definitions.vm.UserLoginVM
import hr.vgabriel.skillconnect.ui.elements.shared.Header
import hr.vgabriel.skillconnect.ui.elements.shared.IconColumn
import hr.vgabriel.skillconnect.ui.elements.shared.MyOutlinedButton
import hr.vgabriel.skillconnect.ui.elements.shared.MyTextField
import hr.vgabriel.skillconnect.ui.wrappers.ScrollableContent


@Composable
fun Login() {
    val authService = LocalAuthService.current
    val navService = LocalNavService.current
    val toastService = LocalToastService.current
    val loadingService = LocalLoadingService.current

    val userLoginVM = remember { UserLoginVM() }

    var performLogin = remember { mutableStateOf(false) }
    LaunchedEffect(performLogin.value) {
        if (performLogin.value) {
            loadingService.delegateLoad {
                try {
                    val isLogin = authService.login(userLoginVM)
                    if (isLogin) {
                        toastService.setToast(
                            ToastData(
                                message = "Login successful!"
                            )
                        )
                        navService.navigateToScreen(Screen.Init)
                    }
                } catch (e: Exception) {
                    toastService.setToast(
                        ToastData(
                            message = "Wrong username or password."
                        )
                    )
                }
            }
        }
        performLogin.value = false;
    }

    Scaffold(
        bottomBar = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                MyOutlinedButton(
                    onClick = {

                        navService.navigateToScreen(Screen.Landing)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Back"
                    )
                    Text("Back")
                }

                MyOutlinedButton(onClick = {
                    performLogin.value = true;

                }) {
                    Text(
                        text = "Login"
                    )
                }
            }
        },
    ) { paddingValues ->
        ScrollableContent(
            innerPadding = paddingValues,
            contents = listOf {
                IconColumn(
                    tint = MaterialTheme.colors.primary,
                    icon = Icons.Default.Login
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Header(title = "Login")

                    MyTextField(
                        value = userLoginVM.emailState.value,
                        onValueChange = { userLoginVM.emailState.value = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MyTextField(
                        value = userLoginVM.passwordState.value,
                        onValueChange = { userLoginVM.passwordState.value = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            }
        )
    }
}
