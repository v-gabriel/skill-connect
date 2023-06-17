package hr.vgabriel.skillconnect.definitions.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import hr.vgabriel.skillconnect.helpers.Validation

val LocalUserVM = staticCompositionLocalOf<UserVM> {
    error("No UserVM provided")
}

open class UserVM : ViewModel() {

    lateinit var userTagsVM: TagsVM

    val nameState: MutableState<String> = mutableStateOf("")
    val surnameState: MutableState<String> = mutableStateOf("")
    val emailState: MutableState<String> = mutableStateOf("")
    val passwordState: MutableState<String> = mutableStateOf("")
    val confirmPasswordState: MutableState<String> = mutableStateOf("")

    val isNameError: Boolean
        get() = nameState.value.isBlank() || nameState.value.length !in 5..50

    val isSurnameError: Boolean
        get() = surnameState.value.isBlank() || surnameState.value.length !in 5..50

    val isEmailError: Boolean
        get() = emailState.value.isBlank() || !Validation.isValidEmail(emailState.value)

    val isPasswordError: Boolean
        get() = passwordState.value.isBlank() || !Validation.isPasswordValid(passwordState.value)

    val isConfirmPasswordError: Boolean
        get() = confirmPasswordState.value.isBlank() || confirmPasswordState.value != passwordState.value

    open val isFormValid: Boolean
        get() = isUserValid && !isPasswordError && !isConfirmPasswordError

    open val isUserValid: Boolean
        get() = !isNameError && !isSurnameError && !isEmailError

    val nameErrorMessage: String
        get() = "Name cannot be empty and must have 5 to 50 characters"

    val surnameErrorMessage: String
        get() = "Surname cannot be empty and must have 5 to 50 characters"

    val emailErrorMessage: String
        get() = "Invalid email"

    val passwordErrorMessage: String
        get() = Validation.getPasswordErrorMessage()

    val confirmPasswordErrorMessage: String
        get() = "Passwords do not match"
}