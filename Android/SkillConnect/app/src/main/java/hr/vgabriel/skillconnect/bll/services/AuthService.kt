package hr.vgabriel.skillconnect.bll.services

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.staticCompositionLocalOf
import hr.vgabriel.skillconnect.MainActivity
import hr.vgabriel.skillconnect.bll.fgservices.ForegroundEventHandlerService
import hr.vgabriel.skillconnect.bll.providers.UserProvider
import hr.vgabriel.skillconnect.bll.repositories.FirebaseRepository
import hr.vgabriel.skillconnect.definitions.models.CommunicationUser
import hr.vgabriel.skillconnect.definitions.vm.UserLoginVM
import hr.vgabriel.skillconnect.definitions.vm.UserRegisterVM
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

interface IAuthService {
    suspend fun login(loginViewModel: UserLoginVM): Boolean
    suspend fun register(registerViewModel: UserRegisterVM): Boolean
    suspend fun logout(): Boolean
}

val LocalAuthService = staticCompositionLocalOf<AuthService> { error("No AuthService provided") }

class AuthService(
    private val repository: FirebaseRepository,
    private val userService: UserService,
    private val azureChatService: AzureChatService,
    private val userProvider: UserProvider,
    private val loggingService: LoggingService
) : IAuthService {

    suspend fun initUser(): Boolean = coroutineScope {
        try {
            val user = repository.authRepository.currentUser
            if (user != null) {
                var userData = userService.getUserData(user.uid)
                if (userData != null) {

                    userProvider.setUser(userData);

                    var communicationUser: CommunicationUser? =
                        azureChatService.refreshAccessToken(userData.communicationUserId)
                            ?: throw Exception("Error refreshing access token");
                    userService.updateCommunicationToken(
                        userData.id,
                        communicationUser!!.accessToken
                    )
                    azureChatService.initChatClient(userData.communicationAccessToken);

                    val serviceIntent = Intent(MainActivity.appActivity, ForegroundEventHandlerService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        MainActivity.appActivity.startForegroundService(serviceIntent)
                    } else {
                        MainActivity.appActivity.startService(serviceIntent)
                    }
                    MainActivity.appActivity.bindService(
                        serviceIntent,
                        MainActivity.appActivity.serviceConnection,
                        Context.BIND_AUTO_CREATE
                    )

                    return@coroutineScope true
                }

                throw Exception("Init error. No user data.")
            }
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[AuthService] initUser", e)
        }
        return@coroutineScope false
    }

    override suspend fun logout(): Boolean = coroutineScope {
        try {
            MainActivity.appActivity.stopAllForegroundServices()

            repository.authRepository.signOut()
            delay(1000)
            repository.authRepository.addAuthStateListener { auth ->
                if (auth.currentUser == null) {
                    MainActivity.restart()
                }
            }

            return@coroutineScope true
        } catch (exception: Exception) {
            loggingService.log(LogType.ERROR, "[AuthService] logout", exception)
        }
        return@coroutineScope false
    }

    override suspend fun login(loginViewModel: UserLoginVM): Boolean = coroutineScope {
        try {
            repository.authRepository.signInWithEmailAndPassword(
                loginViewModel.emailState.value, loginViewModel.passwordState.value
            ).await().user

            return@coroutineScope initUser()
        } catch (exception: Exception) {
            loggingService.log(LogType.ERROR, "[AuthService] login", exception)
        }
        return@coroutineScope false
    }

    override suspend fun register(registerViewModel: UserRegisterVM): Boolean = coroutineScope {
        try {
            val user = repository.authRepository.createUserWithEmailAndPassword(
                registerViewModel.emailState.value, registerViewModel.passwordState.value
            ).await().user
            val communicationUser = azureChatService.getCommunicationUser()

            if (user != null && communicationUser != null) {
                val isSaved =
                    userService.saveNewUserData(user.uid, registerViewModel, communicationUser)
                if (!isSaved) {
                    throw Exception("User data not saved.")
                }
            } else {
                throw Exception(
                    "[AuthService] Error while preparing user account with email: ${registerViewModel.emailState.value}"
                );
            }
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[AuthService] register", e);
            return@coroutineScope false
        }
        return@coroutineScope true
    }
}
