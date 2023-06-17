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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import hr.vgabriel.skillconnect.bll.services.LocalAuthService
import hr.vgabriel.skillconnect.bll.services.LocalLoadingService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LocalToastService
import hr.vgabriel.skillconnect.bll.services.ToastData
import hr.vgabriel.skillconnect.definitions.vm.UserRegisterVM
import hr.vgabriel.skillconnect.ui.elements.form.MyFormTextField
import hr.vgabriel.skillconnect.ui.elements.shared.IconColumn
import hr.vgabriel.skillconnect.ui.elements.shared.MyOutlinedButton
import hr.vgabriel.skillconnect.ui.elements.shared.StepHeader
import hr.vgabriel.skillconnect.ui.wrappers.ScrollableContent

data class RegisterScreenData(val title: String, val content: @Composable () -> Unit)

@Composable
fun Register(
) {
    val authService = LocalAuthService.current
    val navService = LocalNavService.current
    val loadingService = LocalLoadingService.current
    val toastService = LocalToastService.current

    val userRegisterVM = remember { UserRegisterVM() }

    val currentScreen = rememberSaveable { mutableStateOf(0) }

    val registerScreens = arrayOf(
        RegisterScreenData("Step 1: Basic info") { RegisterScreen1(userRegisterVM) },
        RegisterScreenData("Step 2: Password") { RegisterScreen2(userRegisterVM) }
    )

    var performRegister = remember { mutableStateOf(false) }
    LaunchedEffect(performRegister.value) {

        if (performRegister.value && userRegisterVM.isFormValid) {
            loadingService.delegateLoad {
                try {
                    val isRegistered = authService.register(userRegisterVM)
                    if (isRegistered) {
                        toastService.setToast(
                            ToastData(
                                message = "Registration successful!"
                            )
                        )
                        navService.navigateToScreen(Screen.Init)
                    }
                } catch (e: Exception) {
                    toastService.setToast(
                        ToastData(
                            message = "Something went wrong. Please try again later."
                        )
                    )
                    navService.navigateToScreen(Screen.Landing)
                }
            }
        } else if (performRegister.value && !userRegisterVM.isFormValid) {
            toastService.setToast(
                ToastData(
                    message = "Validation failed. Recheck form data."
                )
            )
        }
        performRegister.value = false;
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
                        if (currentScreen.value > 0) {
                            currentScreen.value--
                        } else {
                            navService.navigateToScreen(Screen.Landing)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Back"
                    )
                    Text("Back")
                }

                MyOutlinedButton(
                    onClick = {
                        if (currentScreen.value < registerScreens.size - 1) {
                            currentScreen.value++
                        } else {
                            performRegister.value = true;
                        }
                    },
                ) {
                    Text(
                        text = if (currentScreen.value < registerScreens.size - 1) "Next" else "Register"
                    )
                    if (currentScreen.value < registerScreens.size - 1) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next"
                        )
                    }
                }
            }
        }) { paddingValues ->
        ScrollableContent(
            innerPadding = paddingValues,
            contents = listOf {
                registerScreens[currentScreen.value].content.invoke()
            }
        )
    }
}

@Composable
fun RegisterScreen1(
    userRegisterVM: UserRegisterVM
) {
    IconColumn(
        tint = MaterialTheme.colors.primary,
        icon = Icons.Default.RocketLaunch
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.Top
    ) {
        StepHeader(title = "Step 1: Basic info")

        MyFormTextField(
            value = userRegisterVM.nameState.value,
            onValueChange = { userRegisterVM.nameState.value = it },
            label = "Name",
            isError = userRegisterVM.isNameError,
            errorMessage = userRegisterVM.nameErrorMessage
        )

        Spacer(modifier = Modifier.height(16.dp))

        MyFormTextField(
            value = userRegisterVM.surnameState.value,
            onValueChange = { userRegisterVM.surnameState.value = it },
            label = "Surname",
            isError = userRegisterVM.isSurnameError,
            errorMessage = userRegisterVM.surnameErrorMessage
        )

        Spacer(modifier = Modifier.height(16.dp))

        MyFormTextField(
            value = userRegisterVM.emailState.value,
            onValueChange = { userRegisterVM.emailState.value = it },
            label = "Email",
            isError = userRegisterVM.isEmailError,
            errorMessage = userRegisterVM.emailErrorMessage
        )
    }
}

@Composable
fun RegisterScreen2(
    userRegisterVM: UserRegisterVM
) {
    IconColumn(
        tint = MaterialTheme.colors.primary,
        icon = Icons.Default.Password
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.Top
    ) {
        StepHeader(title = "Step 2: Password")

        MyFormTextField(
            value = userRegisterVM.passwordState.value,
            onValueChange = { userRegisterVM.passwordState.value = it },
            label = "Password",
            isError = userRegisterVM.isPasswordError,
            errorMessage = userRegisterVM.passwordErrorMessage,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        MyFormTextField(
            value = userRegisterVM.confirmPasswordState.value,
            onValueChange = { userRegisterVM.confirmPasswordState.value = it },
            label = "Confirm Password",
            isError = userRegisterVM.isConfirmPasswordError,
            errorMessage = userRegisterVM.confirmPasswordErrorMessage,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
    }
}