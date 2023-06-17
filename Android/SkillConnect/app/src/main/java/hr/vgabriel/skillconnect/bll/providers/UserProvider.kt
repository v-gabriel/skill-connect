package hr.vgabriel.skillconnect.bll.providers

import androidx.compose.runtime.staticCompositionLocalOf
import hr.vgabriel.skillconnect.definitions.models.User
import hr.vgabriel.skillconnect.definitions.vm.UserVM

val LocalUserProvider = staticCompositionLocalOf<UserProvider> { error("No UserProvider provided") }

class UserProvider(
    val userVM: UserVM
) {
    private var _user: User? = null;

    val user: User?
        get() = _user

    fun setUser(user: User?) {
        _user = user
        updateVM()
    }

    fun updateVM() {
        userVM.emailState.value = user!!.email
        userVM.nameState.value = user!!.name
        userVM.surnameState.value = user!!.surname

        userVM.userTagsVM.items.clear()
        userVM.userTagsVM.items.addAll(user!!.tags)
    }
}