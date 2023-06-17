package hr.vgabriel.skillconnect.definitions.vm

import androidx.compose.runtime.mutableStateOf

class UserLoginVM {
    val emailState = mutableStateOf("")
    val passwordState = mutableStateOf("")
}