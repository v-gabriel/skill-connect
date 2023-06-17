package hr.vgabriel.skillconnect.bll.repositories

import androidx.compose.runtime.staticCompositionLocalOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val LocalFirebaseRepository =
    staticCompositionLocalOf<FirebaseRepository> { error("No FirebaseRepository provided") }

class FirebaseRepository(
    private val _authRepository: FirebaseAuth,
    private val _dataRepository: FirebaseFirestore
) {

    var USER_REPOSITORY_KEY = "users"
        private set

    var LOGGING_REPOSITORY_KEY = "logs"
        private set

    var CHAT_THREADS_REPOSITORY_KEY = "chatThreads"
        private set

    var CHAT_THREAD_USERS_REPOSITORY_KEY = "chatThreadUser"
        private set

    val dataRepository: FirebaseFirestore
        get() = _dataRepository

    val authRepository: FirebaseAuth
        get() = _authRepository
}