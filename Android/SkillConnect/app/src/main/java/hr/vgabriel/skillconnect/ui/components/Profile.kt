package hr.vgabriel.skillconnect.ui.components

import Screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hr.vgabriel.skillconnect.bll.providers.LocalUserProvider
import hr.vgabriel.skillconnect.bll.providers.UserProvider
import hr.vgabriel.skillconnect.bll.services.LoadingService
import hr.vgabriel.skillconnect.bll.services.LocalLoadingService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LocalToastService
import hr.vgabriel.skillconnect.bll.services.LocalUserService
import hr.vgabriel.skillconnect.bll.services.NavService
import hr.vgabriel.skillconnect.bll.services.ToastData
import hr.vgabriel.skillconnect.bll.services.ToastService
import hr.vgabriel.skillconnect.bll.services.UserService
import hr.vgabriel.skillconnect.definitions.vm.LocalUserVM
import hr.vgabriel.skillconnect.definitions.vm.UserVM
import hr.vgabriel.skillconnect.ui.elements.form.MyFormTextField
import hr.vgabriel.skillconnect.ui.elements.shared.MyOutlinedButton
import hr.vgabriel.skillconnect.ui.elements.shared.Tags
import hr.vgabriel.skillconnect.ui.elements.shared.UserImage
import hr.vgabriel.skillconnect.ui.wrappers.ScrollableContent

@Composable
fun Profile() {
    val userProvider: UserProvider = LocalUserProvider.current
    val userService: UserService = LocalUserService.current
    val toastService: ToastService = LocalToastService.current
    val loadingService: LoadingService = LocalLoadingService.current
    val navService: NavService = LocalNavService.current

    val userVM: UserVM = LocalUserVM.current

    var isActive = remember { mutableStateOf(false) }

    var performUpdate: MutableState<Boolean> = remember { mutableStateOf(false) }
    LaunchedEffect(performUpdate.value) {
        if (performUpdate.value && userVM.isUserValid) {
            loadingService.delegateLoad {
                try {
                    val result = userService.updateUserData(userProvider.user!!.id, userVM);

                    if (result == true) {
                        toastService.setToast(
                            ToastData(
                                message = "Update successful!"
                            )
                        )
                    }
                } catch (e: Exception) {
                    toastService.setToast(
                        ToastData(
                            message = "Something went wrong. Please try again later."
                        )
                    )
                }
            }
            isActive.value = false
        } else if (performUpdate.value && !userVM.isUserValid) {
            toastService.setToast(
                ToastData(
                    message = "Validation failed. Recheck form data."
                )
            )
        }
        if (performUpdate.value) {
            performUpdate.value = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
                title = { Text(text = "Profile") },
                navigationIcon = {
                    IconButton(onClick = { navService.navigateToScreen(Screen.Settings) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
                    }
                })
        },
        content = { padding ->
            ScrollableContent(modifier = Modifier
                .systemBarsPadding()
                .padding(padding)
                .imePadding(),
                contents = listOf(
                    {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            UserImage(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                initials = userProvider.user!!.initials,
                                size = 56.dp
                            )
                        }

                        Spacer(modifier = Modifier.padding(8.dp))

                        MyFormTextField(
                            value = userVM.nameState.value,
                            onValueChange = { userVM.nameState.value = it },
                            label = "Name",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isActive.value,
                            isError = userVM.isNameError,
                            errorMessage = userVM.nameErrorMessage
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }, {
                        MyFormTextField(
                            value = userVM.surnameState.value,
                            onValueChange = { userVM.surnameState.value = it },
                            label = "Surname",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isActive.value,
                            isError = userVM.isSurnameError,
                            errorMessage = userVM.surnameErrorMessage
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }, {
                        MyFormTextField(
                            value = userVM.emailState.value,
                            onValueChange = { userVM.emailState.value = it },
                            label = "Email",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            isError = userVM.isEmailError,
                            errorMessage = userVM.emailErrorMessage
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }, {
                        Tags(
                            viewModel = userVM.userTagsVM, enabled = isActive.value
                        )
                    })
            )
        }, bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                MyOutlinedButton(
                    modifier = Modifier.padding(0.dp),
                    onClick = {
                        isActive.value = !(isActive.value)
                        userProvider.updateVM()
                    }
                ) {

                    Text(
                        text = if (isActive.value) "Cancel" else "Edit"
                    )
                }
                if (isActive.value) {
                    MyOutlinedButton(
                        modifier = Modifier.padding(
                            top = 0.dp,
                            end = 0.dp,
                            bottom = 0.dp,
                            start = 8.dp
                        ),
                        onClick = {
                            performUpdate.value = true
                        },
                    ) {
                        Text(
                            text = "Update"
                        )
                    }
                }
            }

        })
}


